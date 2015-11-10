/**
 * Created by pradyumna on 02-02-2015.
 */
require(["bootstrap-datepicker"], function() {
    $('#datetimepicker1').datepicker({
        format: "dd/mm/yyyy",
        orientation: "top auto",
        clearBtn: true,
        autoclose: true
    });
});

require(["datatables"], function() {
    $('#example').dataTable();
});
