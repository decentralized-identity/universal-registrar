
var test = require('chloride-test')

//if(process.title === 'node')
  process.exit(
   test(require('./')).fail
  + test(require('./bindings')).fail
  + test(require('./browser-small')).fail
  + test(require('./browser')).fail
  )



