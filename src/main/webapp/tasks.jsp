<%@page %>

<%@include file="header.jsp"%>

<script src="js/http.js"></script>
<link href="css/slide-switch.css" rel="stylesheet">
<link href="css/popup.css" rel="stylesheet">

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

.info-img{width:20px;}

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
            	var info="";
            	if (null!=row['sourceUrl'] || null!=row['hostedUrl']){
            	  info="&nbsp;<a href='#' id='myBtn' onclick='showInfo(\""+row['sourceUrl']+"\",\""+row['hostedUrl']+"\");'><img class='info-img' src='images/info2-512.png'/></a>";
            	}
            	return "<a href='backups.jsp?task="+row['name']+"'>"+row['name']+"</a>"+info;// <div class='popup' onclick=\"document.getElementById(\'myPopup\').classList.toggle(\'show\')\">???</div>";
            }},
            { "targets": 1, "orderable": true, "render": function (data,type,row){
          	  var text="";
          		var x=row['status'].split("|");
          		var status=x[0],responseCode=x[1];
          		
          	  if ('X'==status) text="Unknown";
          	  if ('U'==status) text="Up";
          	  if ('T'==status) text="Unavailable";
          	  if ('D'==status) text="Down";
          	  return "<div class='status status-"+status.toLowerCase()+"' title='Response code was: "+responseCode+"'>"+text+"</div>";
            }},
          { "targets": 2, "orderable": true, "render": function (data,type,row){
        	  var healthIndicator="";
        	  for(i=0;i<row['health'].length;i++)
        		  healthIndicator+="<div class='health health-"+row['health'][i].toLowerCase()+"'>&nbsp;</div>";
        	  return healthIndicator;
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
    	$('#alertsEnabled').prop("checked", "true"==response.toLowerCase());
    });
    // popup info handler
    var modal = document.getElementById('myModal');
		document.getElementsByClassName("close")[0].onclick = function() {
		    modal.style.display = "none";
		}
		window.onclick = function(event) {
		    if (event.target == modal) {
		        modal.style.display = "none";
		    }
		}
});

function showInfo(source, hosted){
	document.getElementById("info-sourceUrl").innerHTML="No Info supplied";
	document.getElementById("info-hostedUrl").innerHTML="No Info supplied";
	
	if (source!="null")
		document.getElementById("info-sourceUrl").innerHTML="<a href='"+source+"'>"+source+"</a>";
	if (hosted!="null")
		document.getElementById("info-hostedUrl").innerHTML="<a href='"+hosted+"'>"+hosted+"</a>";
	document.getElementById("myModal").style.display="block";
}
</script>
  
    <%@include file="nav.jsp"%>
    
    <div id="tasks">
        <div id="buttonbar" style="width:80%;position:relative;top:25px;z-index:1">
        	
        	<!-- Info box -->
					<div id="myModal" class="modal">
				  <div class="modal-content">
				    <span class="close">&times;</span>
				    <table style="width:80%">
				    	<tr><td>Source URL:</td><td><span id='info-sourceUrl'></span></td></tr>
				    	<tr><td>Hosted URL:</td><td><span id='info-hostedUrl'></span></td></tr>
				    </table>
				  </div>
				
				</div>
        	
        	<!-- alerts toggle switch -->
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
        
        <!-- table of tasks -->
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
