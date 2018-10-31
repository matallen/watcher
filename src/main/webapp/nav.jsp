    
<div class="navbar">
  <div class="navbar-header">
    <a href="<%=request.getContextPath()%>" class="navbar-brand2"><img src="images/redhat-logo.png"/>
    	
    	<span class="navbar-brand3">Services - Watcher</span>
    	
    </a>
  </div>
  <div class="collapse navbar-collapse bs-example-js-navbar-collapse">
    <ul class="nav navbar-nav">
      <li class="dropdown">
        <a aria-expanded="false" aria-haspopup="true" role="button" data-toggle="dropdown" class="dropdown-toggle" href="#" id="drop2">
          Backup
          <span class="caret"></span>
        </a>
        <ul aria-labelledby="drop2" class="dropdown-menu">
          <li><a href="events.jsp">Events</a></li>
          <li><a href="config.jsp">Config (Here be dragons!)</a></li>
          <li><a href="database.jsp">Database (Here be dragons!)</a></li>
        </ul>
      </li>
    </ul>
    
    <ul class="nav navbar-nav navbar-right">
      <li class="dropdown" id="fat-menu">
        <a aria-expanded="false" aria-haspopup="true" role="button" data-toggle="dropdown" class="dropdown-toggle" href="#" id="drop3">
        </a>
        <ul aria-labelledby="drop3" class="dropdown-menu">
          <li><a href="#">Settings</a></li>
          <li class="divider" role="separator"></li>
          <li><a href="#">Log out</a></li>
        </ul>
      </li>
    </ul>
  </div>
</div>