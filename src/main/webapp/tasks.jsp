<%@page import="
java.util.Date,
java.util.Calendar,
java.util.Map,
java.io.ObjectInputStream
"%>

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

var taskInfo=[];

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
	        { "data": "enabled" },
	        { "data": "status" },
	        { "data": "health" },
        ],"columnDefs": [
            { "targets": 0, "orderable": true, "render": function (data,type,row){
            	// store the info where it can be accessed for display purposes later
            	taskInfo[row['name']]=row.info;
            	taskInfo[row['name']]['backup']=row['backup'];
            	// add the button to show the info when clicked on
            	var info="&nbsp;<a href='#' id='myBtn' onclick=\"showInfo2('"+row['name']+"')\"><img class='info-img' src='images/info2-512.png'/></a>";
            	console.log("backup="+row['backup']);
            	if (null!= row['backup'] && "true"==row['backup'].toLowerCase()){
	            	return "<a href='backups.jsp?task="+row['name']+"'>"+row['name']+"</a>"+info;
            	}else{
            		return row['name']+info;
            	}
            }},
            { "targets": 1, "orderable": true, "render": function (data,type,row){
            	return "<label class=\"switch-s\"><input onclick=\"toggleTask(this, '"+row['name']+"')\" id=\""+row['name']+"-enabled\" type=\"checkbox\" "+(row['enabled']=="true"?"checked":"")+"><span class=\"slider-s round\"></span></label>";
            	return "";
            }},
            { "targets": 2, "orderable": true, "render": function (data,type,row){
          	  var text="";
          		var x=row['status'].split("|");
          		var status=x[0],responseCode=x[1];
          		
          	  if ('X'==status) text="Unknown";
          	  if ('U'==status) text="Up";
          	  if ('T'==status) text="Unavailable";
          	  if ('D'==status) text="Down";
          	  return "<div class='status status-"+status.toLowerCase()+"' title='Response code was: "+responseCode+"'>"+text+"</div>";
            }},
          { "targets": 3, "orderable": true, "render": function (data,type,row){
        	  var healthIndicator="";
        	  for(i=0;i<row['health'].length;i++)
        		  healthIndicator+="<div class='health health-"+row['health'][i].toLowerCase()+"'>&nbsp;</div>";
        	  return healthIndicator;
          }}
        ]
    } );
}

function toggleTask(o, name){
	var enabled=o.checked;
	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+name+"/enabled/"+enabled, null);
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

function showInfo2(taskName){
	var content="";
	for (key in taskInfo[taskName]) {
		var value=taskInfo[taskName][key]
		if (null==value) value="No info supplied"
		content+="<div class='row'><div class='col-sm-2'>"+key+"</div><div class='col-sm-8'>"+urlify(value)+"</div></div>";
	}
//	if (taskInfo[taskName]['backup']=="true"){
//	  content+="<input type='button' onclick='return backupNow(\""+taskName+"\");' value='Backup Now' />"
//	}
	$('#info').html(content+"");
	document.getElementById("myModal").style.display="block";
}

function urlify(text) {
	var urlRegex = /(http.+[^\s])/g;
	return text.replace(urlRegex, function(url) {
       //return '<a href="${pageContext.request.contextPath}/api/download?file=' + url + '">' + url + '</a>';
       console.log("url="+url);
		return '<a href="'+url+'">'+url+'</a>';
   })
}

//function backupNow(taskName){
//	console.log("BackupNow:: "+taskName);
//	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+taskName+"/backupNow", null);
//}
</script>
  
    <%@include file="nav.jsp"%>
    
    <div id="tasks">
        <div id="buttonbar" style="width:80%;position:relative;top:25px;z-index:1">
        	
        	<!-- Info box -->
					<div id="myModal" class="modal">
				  <div class="modal-content">
				    <span class="close">&times;</span>
					    <div id="info"></div>
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
                      <th align="left">Enabled</th>
                      <th align="left">Status</th>
                      <th align="left">Health</th>
                  </tr>
              </thead>
          </table>
        </div>
    </div>
