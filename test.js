

var oo =
{
"graphdata" : {
	"nodes" : [
	
	    {
		    "name" : "TOMCAT11",
		    "hosts" : [
		    
		    ],
		    "serviceType" : "TOMCAT",
		    "terminal" : "false"
	    } ,
	
	    {
		    "name" : "hippo",
		    "hosts" : [
		    
		        "10.98.133.22:3306"
		        
		    
		    ],
		    "serviceType" : "MYSQL",
		    "terminal" : "true"
	    } ,
	
	    {
		    "name" : "TOMCAT11",
		    "hosts" : [
		    
		    ],
		    "serviceType" : "TOMCAT",
		    "terminal" : "false"
	    } ,
	
	    {
		    "name" : "MEMCACHED",
		    "hosts" : [
		    
		        "10.25.149.80:11211"
		        
		    
		    ],
		    "serviceType" : "MEMCACHED",
		    "terminal" : "true"
	    } ,
	
	    {
		    "name" : "dev",
		    "hosts" : [
		    
		        "10.25.149.80:11211"
		        
		    
		    ],
		    "serviceType" : "ARCUS",
		    "terminal" : "true"
	    } 
	
	],
	"links" : [
	
	    {
			"source" : 0,
			"target" : 3,
			"value" : 1,
			"error" : 0,
			"slow" : 0,
			"histogram" : { "100" : 1, "300" : 0, "500" : 0 }
		} ,
	
	    {
			"source" : 0,
			"target" : 4,
			"value" : 1,
			"error" : 0,
			"slow" : 0,
			"histogram" : { "100" : 1, "300" : 0, "500" : 0 }
		} ,
	
	    {
			"source" : 0,
			"target" : 2,
			"value" : 4,
			"error" : 0,
			"slow" : 0,
			"histogram" : { "1000" : 4, "3000" : 0, "5000" : 0 }
		} ,
	
	    {
			"source" : 0,
			"target" : 1,
			"value" : 3,
			"error" : 0,
			"slow" : 0,
			"histogram" : { "1000" : 3, "3000" : 0, "5000" : 0 }
		} 
	
	]
	}
};