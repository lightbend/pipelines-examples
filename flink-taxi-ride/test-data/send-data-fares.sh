for str in $(cat nycTaxiFares.json)
do
  echo "Using $str"
  curl -i -X POST taxi-ride-fare.apps.purplehat.lightbend.com/taxi-fare -H "Content-Type: application/json" --data "$str"
done

