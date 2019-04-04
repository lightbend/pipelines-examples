
# coding: utf-8

# In[1]:


import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import uuid
import pickle
import base64
import json
import datetime
import avro.schema
import avro.io

import io, random

from avro.datafile import DataFileReader, DataFileWriter
from avro.io import DatumReader, DatumWriter
from keras.models import Model
from keras.layers import *
from keras.losses import *
import tensorflow as tf
from tensorflow.python.tools import freeze_graph
from tensorflow.python.tools import optimize_for_inference_lib
from keras import backend as K


# In[2]:


FILE_PATH = 'data/'


# In[3]:


original_dataset = pd.read_csv(FILE_PATH + 'ratings_uuid_small.csv')


# In[4]:


original_dataset.head()


# In[5]:


# simulating real world data - replace customer id and product id with unique UUIDs

dataset = original_dataset

# customerId = dataset['customerId'].unique().tolist()
# uuids = []
# for index in range (0, len(customerId)):
#     uuids.append(str(uuid.uuid4()))
    
# mapping = dict( zip(customerId, uuids) )
# dataset.replace({'customerId': mapping},inplace=True)

# productId = dataset['productId'].unique().tolist()
# uuids = []
# for index in range (0, len(productId)):
#     uuids.append(str(uuid.uuid4()))
    
# mapping = dict( zip(productId, uuids) )
# dataset.replace({'productId': mapping},inplace=True)

# dataset.to_csv("ratings_with_uuid.csv")


# ## Convert Customer and Product Ids to integers

# In[6]:


# now we have the dataset like the actual dataset with strings in customerId and productId
# replace each with integers

customerIds = dataset['customerId'].unique().tolist()
customerMapping = dict( zip(customerIds, range(len(customerIds))) )
dataset.replace({'customerId': customerMapping},inplace=True)

productIds = dataset['productId'].unique().tolist()
productMapping = dict( zip(productIds, range(len(productIds))) )
dataset.replace({'productId': productMapping},inplace=True)


# In[7]:


customer_idxs = np.array(dataset.customerId, dtype = np.int)
product_idxs = np.array(dataset.productId, dtype = np.int)

ratings = np.array(dataset.scaled_purchase_freq)


# ## Data Pre-processing

# In[8]:


n_customers = int(dataset['customerId'].drop_duplicates().max()) + 1
n_products = int(dataset['productId'].drop_duplicates().max()) + 1
n_factors = 50

input_shape = (1,)


# In[9]:


print(n_customers)
print(n_products)


# ## Tensorflow Session

# In[10]:


# create TF session and set it in Keras
sess = tf.Session()
K.set_session(sess)
K.set_learning_phase(1)


# ## The Model

# In[11]:


class DeepCollaborativeFiltering(Model):
    def __init__(self, n_customers, n_products, n_factors, p_dropout = 0.2):
        x1 = Input(shape = (1,), name="user")

        P = Embedding(n_customers, n_factors, input_length = 1)(x1)
        P = Reshape((n_factors,))(P)

        x2 = Input(shape = (1,), name="product")

        Q = Embedding(n_products, n_factors, input_length = 1)(x2)
        Q = Reshape((n_factors,))(Q)

        x = concatenate([P, Q], axis=1)
        x = Dropout(p_dropout)(x)

        x = Dense(n_factors)(x)
        x = Activation('relu')(x)
        x = Dropout(p_dropout)(x)

        output = Dense(1)(x)       
        
        super(DeepCollaborativeFiltering, self).__init__([x1, x2], output)
    
    def rate(self, customer_idxs, product_idxs):
        if (type(customer_idxs) == int and type(product_idxs) == int):
            return self.predict([np.array(customer_idxs).reshape((1,)), np.array(product_idxs).reshape((1,))])
        
        if (type(customer_idxs) == str and type(product_idxs) == str):
            return self.predict([np.array(customerMapping[customer_idxs]).reshape((1,)), np.array(productMapping[product_idxs]).reshape((1,))])
        
        return self.predict([
            np.array([customerMapping[customer_idx] for customer_idx in customer_idxs]), 
            np.array([productMapping[product_idx] for product_idx in product_idxs])
        ])


# ## Hyperparameters

# In[12]:


bs = 64
val_per = 0.25
epochs = 1


# In[13]:


model = DeepCollaborativeFiltering(n_customers, n_products, n_factors)


# In[14]:


model.summary()


# ## Training

# In[15]:


model.compile(optimizer = 'adam', loss = mean_squared_logarithmic_error)


# In[16]:


model.fit(x = [customer_idxs, product_idxs], y = ratings, batch_size = bs, epochs = epochs, validation_split = val_per)


# In[17]:


model.rate(9, 0)


# In[18]:


model.rate('e9a87a97-38df-4858-86df-9b02defccd5c', '9910eb1b-9d99-4025-badc-13ef455bb49a')


# In[19]:


model.rate(25, 0)


# In[20]:


model.output[1].name


# ## Save Tensorflow Model

# In[21]:


print('Done training!')

print ("input 0", model.input[0].name)
print ("input 1", model.input[1].name)
print ("input ", model.input)

print ("output 0", model.output[0].name)
print ("output 1", model.output[1].name)
print ("output", model.output)

# create the saver
# Saver op to save and restore all the variables
saver = tf.train.Saver()


# In[22]:


# Save produced model
model_path = "/Users/debasishghosh/models/"
model_name = "ProductRecommender"
save_path = saver.save(sess, model_path+model_name+".ckpt")
print ("Saved model at ", save_path)
graph_path = tf.train.write_graph(sess.graph_def, model_path, model_name+".pb", as_text=True)
print ("Saved graph at :", graph_path)


# ## Freeze Computation Graph

# In[23]:


# Now freeze the graph (put variables into graph)

input_saver_def_path = ""
input_binary = False
output_node_names = "dense_2/BiasAdd"          # Model result node

restore_op_name = "save/restore_all"
filename_tensor_name = "save/Const:0"
output_frozen_graph_name = model_path + 'frozen_' + model_name + '.pb'
clear_devices = True


freeze_graph.freeze_graph(graph_path, input_saver_def_path,
                         input_binary, save_path, output_node_names,
                         restore_op_name, filename_tensor_name,
                         output_frozen_graph_name, clear_devices, "")
print ("Model is frozen")


# ## Optimize and Save Optimzed Graph

# In[24]:


# optimizing graph

input_graph_def = tf.GraphDef()
with tf.gfile.Open(output_frozen_graph_name, "rb") as f:
   data = f.read()
   input_graph_def.ParseFromString(data)


output_graph_def = optimize_for_inference_lib.optimize_for_inference(
   input_graph_def,
   ['user', 'product'],      # an array of the input node(s)
   ["dense_2/BiasAdd"],      # an array of output nodes
   tf.float32.as_datatype_enum)


# In[25]:


[node.op.name for node in model.outputs]


# In[26]:


[node.op.name for node in model.inputs]


# In[27]:


# Save the optimized graph

tf.train.write_graph(output_graph_def, model_path, "optimized_" + model_name + ".pb", as_text=False)
tf.train.write_graph(output_graph_def, model_path, "optimized_text_" + model_name + ".pb", as_text=True)


# ## Read optimized graph as binary

# In[28]:


with open(model_path + "optimized_" + model_name + ".pb", "rb") as f:
    model_file_binary = f.read()


# ## Generate Model Id

# In[29]:


## generate a model Id based on current timestamp
model_id = 'recommender-model-' + '{:%Y-%m-%d-%H:%M:%S}'.format(datetime.datetime.now())


# ## Generate Avro with Schema

# In[30]:


## Generate avro directly

# Parse the schema file
schema = avro.schema.Parse(open("avro/RecommenderModel.avsc", "rb").read())

# Create a data file using DataFileWriter
dataFile = open(model_path + "recommender.avro", "wb")

writer = DataFileWriter(dataFile, DatumWriter(), schema)

# Write data using DatumWriter
writer.append({"modelId": model_id,
               "tensorFlowModel": model_file_binary,
               "productMap": productMapping,
               "customerMap": customerMapping
              })

writer.close()


# In[31]:


reader = DataFileReader(open(model_path + "recommender.avro", "rb"), DatumReader())
for model in reader:
    r = model
reader.close()


# In[32]:


type(r)


# In[33]:


r.keys()


# In[34]:


r["modelId"]


# In[35]:


r["productMap"]


# ## Generate Avro Schemaless

# In[36]:


writer = avro.io.DatumWriter(schema)

bytes_writer = io.BytesIO()
encoder = avro.io.BinaryEncoder(bytes_writer)
    
# Write data using DatumWriter
writer.write({"modelId": model_id,
              "tensorFlowModel": model_file_binary,
              "productMap": productMapping,
              "customerMap": customerMapping
              }, encoder)
raw_bytes = bytes_writer.getvalue()

open(model_path + "recommender-no-schema.avro", 'wb').write(raw_bytes)

bytes_reader = io.BytesIO(raw_bytes)
decoder = avro.io.BinaryDecoder(bytes_reader)
reader = avro.io.DatumReader(schema)
r = reader.read(decoder)
r["productMap"]

