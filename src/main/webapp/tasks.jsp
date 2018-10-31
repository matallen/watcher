<%@page %>

<%@include file="header.jsp"%>
<%@include file="datatables-dependencies.jsp"%>

<script>

function loadDataTable(){
  $('#example').DataTable( {
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/tasks/',
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
//        ],"columnDefs": [
//          { "targets": 2, "orderable": true, "render": function (data,type,row){
//        	  console.log(urlify2(row['text']));
//        		  return urlify2(row['text']);
//          }}
        ]
    } );
}

//function urlify(text) {
//   var urlRegex = /(https?:\/\/[^\s]+)/g;
//   return text.replace(urlRegex, function(url) {
//       return '<a href="' + url + '">' + url + '</a>';
//   })
//   // or alternatively
//   // return text.replace(urlRegex, '<a href="$1">$1</a>')
//}
//function urlify2(text) {
//	var urlRegex = /([^\s]+.bak+)/g;
//	return text.replace(urlRegex, function(url) {
//       return '<a href="${pageContext.request.contextPath}/api/download?file=' + url + '">' + url + '</a>';
//   })
//}


$(document).ready(function() {
    loadDataTable();
});
</script>
  
    <%@include file="nav.jsp"%>
    
    <div id="tasks">
        <div id="tasks">
        </div>
        <div id="tableDiv">
          <table id="example" class="display" cellspacing="0" width="100%">
              <thead>
                  <tr>
                      <th align="left">Name</th>
                      <th align="left">Status</th>
                      <th align="left">Health</th>
                      <th align="left"></th>
                  </tr>
              </thead>
          </table>
        </div>
    </div>

