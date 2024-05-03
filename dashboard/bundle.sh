npm run build-dashboard
rm -r build/static
mkdir build/static

cp build/out.* build/static
cp -r public/* build/static
