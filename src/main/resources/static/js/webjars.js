/**
 * Created by pradyumna on 03-02-2015.
 */
var webjars = {
    versions: {"bootstrap":"3.3.2","bootstrap-datepicker":"1.3.1","jquery-ui-themes":"1.11.2","jquery-ui":"1.11.2","requirejs":"2.1.15","jquery":"2.1.3","bootstrap-modal":"2.2.5"},
    path: function(webJarId, path) {
        alert('webjars '+webJarId);
        console.error('The webjars.path() method of getting a WebJar path has been deprecated.  The RequireJS config in the ' + webJarId + ' WebJar may need to be updated.  Please file an issue: http://github.com/webjars/' + webJarId + '/issues/new');
        return ['/pla/webjars/' + webJarId + '/' + webjars.versions[webJarId] + '/' + path];
    }
};

var require = {
    callback: function() {
        // Deprecated WebJars RequireJS plugin loader
        define('webjars', function() {
            return {
                load: function(name, req, onload, config) {
                    alert('load');
                    if (name.indexOf('.js') >= 0) {
                        console.warn('Detected a legacy file name (' + name + ') as the thing to load.  Loading via file name is no longer supported so the .js will be dropped in an effort to resolve the module name instead.');
                        name = name.replace('.js', '');
                    }
                    console.error('The webjars plugin loader (e.g. webjars!' + name + ') has been deprecated.  The RequireJS config in the ' + name + ' WebJar may need to be updated.  Please file an issue: http://github.com/webjars/webjars/issues/new');
                    req([name], function() {;
                        onload();
                    });
                }
            }
        });

        // All of the WebJar configs
        requirejs.config({"paths":{"bootstrap":["/pla/webjars/bootstrap/3.3.2/js/bootstrap","js/bootstrap"],"bootstrap-css":["/pla/webjars/bootstrap/3.3.2/css/bootstrap","css/bootstrap"]},"shim":{"bootstrap":["jquery"]}})
        requirejs.config({"paths":{"bootstrap-datepicker":["/pla/webjars/bootstrap-datepicker/1.3.1/js/bootstrap-datepicker","js/bootstrap-datepicker"]},"shim":{"bootstrap-datepicker":["bootstrap"]}})
        requirejs.config({"paths":{"jquery-ui":["/pla/webjars/jquery-ui/1.11.2/jquery-ui","jquery-ui"],"jquery-ui-min":["/pla/webjars/jquery-ui/1.11.2/jquery-ui.min","jquery-ui.min"]},"shim":{"jquery-ui":["jquery"],"jquery-ui-min":["jquery"]}})
        requirejs.config({"paths":{}})
        requirejs.config({"paths":{"jquery":["/pla/webjars/jquery/2.1.3/jquery","jquery"]},"shim":{"jquery":{"exports":"$"}}})
    }
}