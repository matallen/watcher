<%@page %>

<%@include file="header.jsp"%>
<script src="js/http.js"></script>

<style>
#backups{margin:auto;width:95%;}
</style>
<script>
var table;
function loadDataTable(){
  table=$('#example').DataTable( {
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/backups/'+Utils.getParameterByName("task"),
            "dataSrc": ""
        },
        "scrollY":        "1300px",
        "scrollCollapse": true,
        "paging":         false,
        "lengthMenu":     [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
        "pageLength" :    5, // default page entries
        "order" :         [[1,"desc"]],
        "columns": [
          { "data": "name" },
        ],"columnDefs": [
            { "targets": 0, "orderable": false, "render": function (data,type,row){
                return "<input type='checkbox' name='cbx_"+row['name']+"' value='"+row['name']+"'></input>";
            }},
            { "targets": 1, "orderable": true, "render": function (data,type,row){
                return "<a href='api/download?file="+Utils.getParameterByName("task")+"/"+row['name']+"'>"+row['name']+"</a>";
            }},
            { "targets": 2, "orderable": true, "render": function (data,type,row){
                return row['size'];
            }},
        ]
    });
}

//function urlify2(text) {
//	var urlRegex = /([^\s]+.bak+)/g;
//	return text.replace(urlRegex, function(url) {
//       return '<a href="${pageContext.request.contextPath}/api/download?file=' + url + '">' + url + '</a>';
//   })
//}


$(document).ready(function() {
    loadDataTable();
});
function backupNow(taskName){
	console.log("BackupNow:: "+taskName);
	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+taskName+"/backupNow", null, function(response){
		//console.log("XXX status="+response.status);
		table.ajax.reload();
	});
}
function selectAll(caller){
	$('input[name^="cbx_"]').each(function(idx, cbx){
		cbx.checked=caller.checked;
	});
}
function deleteAll(taskName){
	var list=[],i=0;
	$('input[name^="cbx_"]:checked').each(function(idx, cbx){
		list[i++]=cbx.value;//=caller.checked;
		Http.httpDelete("${pageContext.request.contextPath}/api/tasks/"+taskName+"/backups/"+cbx.value+"/delete", function(){
			console.log("delete sent for: "+cbx.value);
		});
	});
	table.ajax.reload();
	//console.log("list="+list);
}
</script>
  
    <%@include file="nav.jsp"%>
    
    <input type="button" onclick="return backupNow(Utils.getParameterByName('task'))" value="Backup Now"/>
    <input type="button" onclick="return deleteAll(Utils.getParameterByName('task'));" value="Delete"/>
    
    
    <div id="backups">
        <div id="buttonbar"></div>
        <div id="tableDiv">
          <table id="example" class="display">
              <thead>
                  <tr>
                      <th style="width:10px;" align="left"><input type="checkbox" onclick="return selectAll(this);"/></th>
                      <th align="left">Backups</th>
                      <th align="left">Size</th>
                  </tr>
              </thead>
          </table>
        </div>
    </div>

