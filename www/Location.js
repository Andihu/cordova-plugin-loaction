var exec = require('cordova/exec');

exports.getLocation = function (arg0, success, error) {
    exec(success, error, 'Location', 'getLocation', [arg0]);
};
exports.openLocation = function (arg0,success, error) {
    exec(success, error, 'Location', 'openLocation', [arg0]);
};