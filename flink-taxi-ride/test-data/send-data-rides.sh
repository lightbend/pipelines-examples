for str in $(cat nycTaxiRides.json)
do
  echo "Using $str"
  curl -i -X POST taxi-ride-fare.apps.purplehat.lightbend.com/taxi-ride -H "Content-Type: application/json" --data "$str"
done
