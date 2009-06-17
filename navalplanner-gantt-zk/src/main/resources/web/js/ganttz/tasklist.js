HEIGHT_PER_ROW = 30;
HEIGHT_TIME_TRACKER = 130;

zkTasklist = {};

zkTasklist.init = function(cmp) {
    zkTasklist.adjust_height(cmp);
}

zkTasklist.adjust_height = function(cmp) {
    var height = cmp.style.height;
    var component_to_adjust = document.getElementById(cmp
            .getAttribute('sameHeightElementId'));

    function setHeight(element, offset) {
        if (!offset) {
            offset = 0;
        }
        var newheigth = parseInt(height) + offset;
        element.style["height"] = newheigth + 'px';
    }

    setHeight(document.getElementById('ganttpanel'), HEIGHT_TIME_TRACKER);
    if (component_to_adjust) {
        setHeight(component_to_adjust);
    }
    var found = YAHOO.util.Selector.query(".fake_column", component_to_adjust,
            false);
    found.each( function(element) {
        setHeight(element,HEIGHT_PER_ROW);
    });
}
