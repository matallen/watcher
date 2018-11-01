<%@page %>

<%@include file="header.jsp"%>

<script src="js/http.js"></script>
<link href="css/slide-switch.css" rel="stylesheet">

<style>
#tasks{margin:auto;width:95%;}
#buttonbar{position:relative;}
.status{width: 150px;text-align: center;border-radius:10px;color:#FFF}
.status-u{background-color: #71b568;}
.status-d{background-color: #EE0000;}
.status-t{background-color: #f19c48;}
.status-x{background-color: #ddd;}

.health{width: 20px;height:20px;float:left;border: silver solid 1px;}
.health-u{background-color: #71b568;}
.health-d{background-color: #EE0000;}
.health-t{background-color: #f19c48;}
.health-x{background-color: #ddd;}
/*
*/

</style>
<script>

function loadDataTable(){
  $('#example').DataTable( {
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/tasks',
            "dataSrc": ""
        },
        "scrollY":        "1300px",
        "scrollCollapse": true,
        "paging":         false,
        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
        "pageLength" : 5, // default page entries
        "order" : [[0,"desc"]],
        "columns": [
	        { "data": "name" },
	        { "data": "status" },
	        { "data": "health" },
        ],"columnDefs": [
            { "targets": 0, "orderable": true, "render": function (data,type,row){
          	  return "<a href='backups.jsp?task="+row['name']+"'>"+row['name']+"</a>";
            }},
            { "targets": 1, "orderable": true, "render": function (data,type,row){
          	  var result="";
          	  if ('X'==row['status']) result="Unknown";
          	  if ('U'==row['status']) result="Up";
          	  if ('T'==row['status']) result="Timeout";
          	  if ('D'==row['status']) result="Down";
          	  return "<div class='status status-"+row['status'].toLowerCase()+"'>"+result+"</div>";
            }},
          { "targets": 2, "orderable": true, "render": function (data,type,row){
        	  var result="";
        	  var i;
        	  for(i=0;i<row['health'].length;i++){
        		  result+="<div class='health health-"+row['health'][i].toLowerCase()+"'>&nbsp;</div>";
        	  }
        	  return result;
          }}
        ]
    } );
}

$(document).ready(function() {
    loadDataTable();
    
    // onchange handler to set or unset the config option
    $('#alertsEnabled').change(function() {
    	var enabled=$(this).is(':checked');
    	Http.httpPost("${pageContext.request.contextPath}/api/config/options/slack.webhook.notifications", enabled);
    });
    // set the initial value from the server-side config
    Http.httpGet("${pageContext.request.contextPath}/api/config/options/slack.webhook.notifications", function(response){
    	$('#alertsEnabled').prop("checked", response);
    });
});
</script>
  
    <%@include file="nav.jsp"%>
    
    <div id="tasks">
        <div id="buttonbar" style="width:80%;position:relative;top:25px;z-index:1">
        	
        	<div style="position:relative; top:0px;">
						<label class="switch">
						  <input id="alertsEnabled" type="checkbox">
						  <span class="slider round"></span>
						</label>
						<span style="font-size:12pt;position:relative;top:15px;left:5px;">
		        	<label for="alertsEnabled">Alerts Enabled</label>
						</span>
        	</div>
        	
        </div>
        <div id="tableDiv">
          <table id="example" class="display">
              <thead>
                  <tr>
                      <th align="left">Name</th>
                      <th align="left">Status</th>
                      <th align="left">Health</th>
                  </tr>
              </thead>
          </table>
        </div>
    </div>
