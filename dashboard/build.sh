npm run build
STATUS_CODE=$?

if [[ $STATUS_CODE != 0 ]]; then 
  echo "Build failed with exit code $STATUS_CODE"
  exit $STATUS_CODE
fi

mv build dashboard
mkdir -p build/static
mv dashboard build/static/dashboard
