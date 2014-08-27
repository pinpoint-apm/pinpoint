coffee -c src/angular-slider.coffee

mv src/angular-slider.js ./

uglifyjs angular-slider.js -mc > angular-slider.min.js

stylus src/angular-slider.styl -c --use ./node_modules/nib -o ./
mv angular-slider.css angular-slider.min.css
stylus src/angular-slider.styl --use ./node_modules/nib -o ./
