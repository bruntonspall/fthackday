cubism_contextPrototype.hack = function(host) {
    if (!arguments.length) host = "";
    var source = {},
        context = this;

    source.metric = function(expression) {
        return context.metric(function(start, stop, step, callback) {
            d3.json(host + "/metric"
                + "?expression=" + encodeURIComponent(expression)
                + "&start=" + cubism_cubeFormatDate(start)
                + "&stop=" + cubism_cubeFormatDate(stop)
                + "&step=" + step, function(data) {
                if (!data) return callback(new Error("unable to load data"));
                callback(null, data.map(function(d) { return d.value; }));
            });
        }, expression += "");
    };

    // Returns the Cube host.
    source.toString = function() {
        return host;
    };

    return source;
};
