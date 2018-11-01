<html>
<head>
   <meta http-equiv="cache-control" content="no-cache"/>
   <meta charset="utf-8"/>
   <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
   <meta name="viewport" content="width=device-width, initial-scale=1"/>
   
   <title>Watcher</title>
   
   <link href="css/jquery.dataTables.min.css" rel="stylesheet">
   <link href="css/bootstrap.min.css" rel="stylesheet">
   <link rel="stylesheet" href="css/style2.css" type="text/css">
   
   <script src="js/jquery-1.11.3.min.js"></script>
   <script src="js/jquery.dataTables.min.js"></script>
   <script src="js/bootstrap.min.js"></script>
   
   <!--
   <script src="js/ChartNew.js"></script>
   -->
	
	<script>
		Utils = {
				getParameterByName: function(name, url) {
					  if (!url) url = window.location.href;
					  name = name.replace(/[\[\]]/g, "\\$&");
					  var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
					      results = regex.exec(url);
					  if (!results) return null;
					  if (!results[2]) return '';
					  return decodeURIComponent(results[2].replace(/\+/g, " "));
					}
			}
	</script>
</head>