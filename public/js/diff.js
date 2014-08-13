(function() {
    var oldtxt = difflib.stringAsLines($("#oldtxt").val());
    var newtxt = difflib.stringAsLines($("#newtxt").val());
    var sm = new difflib.SequenceMatcher(oldtxt, newtxt);
    var opcodes = sm.get_opcodes();
    var diffdiv = $("#diffdiv");
    diffdiv.append(diffview.buildView({
        baseTextLines: oldtxt,
        newTextLines: newtxt,
        opcodes: opcodes,
        baseTextName: "Old",
        newTextName: "New",
        contextSize: 4,
        viewType: 1
    }));

    $("td.skip").append("...");
    $("#diffdiv thead th").not(".texttitle").remove();
    $(".texttitle")
        .attr("colspan", 3)
        .html($("#difftitle").html());
})();
