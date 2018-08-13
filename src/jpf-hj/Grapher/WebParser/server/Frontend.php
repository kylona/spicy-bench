<?php
  //get the mainclass and jar file given by the user
  //Global variables
  $mainclass = $_POST['mainclass']; //Also the name of the config file
  $javaText = $_POST['javacode'];
  $absolutePath = getcwd();
  //echo "hello";
  /**
  * Read Text saves text submitted as a JAVA file and then generates a config
  * @param $javacode
  * The java code submitted by the user
  */
  function readText($javaCode){
      $myfile = fopen($_POST['mainclass'] . ".java", "w") or die("Unable to open file!");
      //echo $javaCode . "FUNCTION";
      fwrite($myfile, $javaCode);
      fclose($myfile);

    }
    /**
    * Grab the dot file that has the related graph data and turn it into string format
    * to be given to the user.
    * @param mainclass <- the name of the main class that was given as well as the associated file path that is connected to get to the java class within the JAR file.
    * TODO parse through the file directories and figure out where the class files are.
    * The path to the files might be included in the manifest.
    */
    function dotToString($mainclassname){
        //open the dot file. this dot file name will be in the format of directory.subdir.subdir.nameofmainclass.dot
        $file = fopen("carTest." . $mainclassname . ".dot", r) or die("unable to file!");

        //string that we will return
        $dotStringFormat;
        //parse file.
        while(!feof($file)){
          $line = fgets($file);
          $dotStringFormat .= $line . "\n";
        }
        //close the file when we are done with it.
        fclose($file);
        return $dotStringFormat;
    }


    class VisibleOnlyFilter extends RecursiveFilterIterator
    {
        public function accept()
        {
            $fileName = $this->getInnerIterator()->current()->getFileName();
            $firstChar = $fileName[0];
            return $firstChar !== '.';
        }
    }

    //Allows us to recurse a file directory
    class FilesOnlyFilter extends RecursiveFilterIterator
    {
        public function accept()
        {
            $iterator = $this->getInnerIterator();

            // allow traversal
            if ($iterator->hasChildren()) {
                return true;
            }

            // filter entries, only allow true files
            return $iterator->current()->isFile();
        }
    }
    // goes through the parse
    function parseFiles($rootpath){
      $fileinfos = new RecursiveIteratorIterator(
        new FilesOnlyFilter(
            new VisibleOnlyFilter(
                new RecursiveDirectoryIterator(
                    $rootpath,
                    FilesystemIterator::SKIP_DOTS
                        | FilesystemIterator::UNIX_PATHS
                )
            )
        ),
        RecursiveIteratorIterator::LEAVES_ONLY,
        RecursiveIteratorIterator::CATCH_GET_CHILD
      );
      $pathToFile;
      $count = 0;
      //Go through each of the subdirectories recursively
      foreach ($fileinfos as $pathname => $fileinfo) {
        //echo $pathname . "\n";
        //break up the file path to get file type
        $filetype = explode(".", $pathname);
        //Get the size of the array
        $size = count($filetype);
        //check to see if the files that are contained in the folder are class files
        //and set the file path that
        //echo $count++;
        if($filetype[$size -1] == "class"){
          //we want the path to be in the form of dir.subdir.subdir
           $pathToClassFiles = explode('/', $fileinfo);
           //print_r($pathToClassFiles);
           //Go through the array and filter out certain cases
           foreach($pathToClassFiles as $dirName => $name){
              //So we want to go through each one and not add, input_classes or any class files
              $fileTypeName = explode(".", $name);
              //print_r($fileTypeName);
              //echo $name;
              $sizeFileCheck = count($fileTypeName);
              //echo $sizeFileCheck;
              //if the size is 1 then it is a directory name and is added to the path name
              if($sizeFileCheck == 1 && $name != $rootpath){
                //check that the filepath is null already and set it rather than adding it on
                if($pathToFile == ""){
                  $pathToFile = $name . ".";
                }else{
                  $pathToFile += $name . ".";
                }
              }
           }
           //I though that it might be necessary to pull off the . from the end but
           //now that I am thinking about it I realize that I will just append the who name onto the
           //mainclass name to get the who path
           //$pathToFile = substr($pathToFile, 0, strlen($pathToFile) - 1);
           break;
        }

      }
      return $pathToFile;
    }


    /**
    * Some static logic the determines what to run depending
    * on if the User input a Java file or simply java code
    * @param $_POST['gender'] or the value of the buttons that helps us see i
    * what to do.
    * if they submit java code we need to compile it into .jar format before we can proceed.
    */
    $pathToMainClass = "";
    if($_POST['gender'] == "source-code"){
      readText($javaText);
      exec("javac " . $mainclass . ".java " . "-d input_classes/");
    }elseif($_POST['gender'] == "file"){
      //first thing that I need to do is to put the jar file into the input_classes folder
      $pathToMainClass = parseFiles("input_classes");
      //echo $pathToMainClass;
      exec("cd input_classes & jar xf ../" . $mainclass . ".jar");
      exec("cd ..");
    }else{
      echo "not working";
    }
      generateConfig($mainclass, $pathToMainClass);

    /**
  * Function generateConfig
  * @param : $filename
  * name of Main class.
  * the JPF should be run on the new config file.jpf ie MainClass.jpf
  */
 function generateConfig($filename, $pathToMain){
     //The file properties.jpf is the file that we use for reference on generating a new file. **IMPORTANT that the path to this file be included so it can be reproduced.
      $myfile = fopen("properties.jpf", "r+");
      $generateNewConfig = 0;

      //The filename should be the name of the mainclass of the JAR file that was submitted in the code.
      $filepathname = $filename . ".jpf";
      $setPermissions = false;

      //check to make sure the that the file does not already exist
      if (!file_exists($filepathname)){
            $setPermissions = true;
      }

      $prefix;
      $files = scandir("input_classes");

      //go through the subdirectories
      //I think that this is what will be going through and getting the directory information needed for the main class
      //make it a function that can be used elsewhere to parse through
      foreach ($files as $key => $value) {
        if (!in_array($value,array(".","..","META-INF"))){
          if (is_dir("input_classes" . DIRECTORY_SEPARATOR . $value)){

          }
          else{
             $result[] = $value;
          }
        }
      }
      //open a new file that can be written.
      $newFile = fopen($filepathname, "w+");
      //get each line and check for certain things
      $native_count = 0;

      //parse through the file and change the lines that are needed.
      while(!feof($myfile)){
          //get the next line in the file
          $line = fgets($myfile);

          //$line is now an array that should contain ["Results_directory", "=", "/filepath"]
          $letters = explode(" ", $line);
          //echo "HERE3";
          //print($letters[0]);
          if(strcmp($letters[0], "target")==0){
                  $letters[2] = $pathToMain . $filename;
          }
         // echo "HERE1\n";
          $output =  implode($letters, " ");
          //print($output);
          fwrite($newFile, $output);
      }
      //echo "end\n";
      //always close things that you start
      fclose($myfile);
      fclose($newFile);
      //in order for the file to accessible later on, we need to set the permissions to read
      if($setPermissions == true){
        chmod($filepathname, 0777);
      }
 }

  //run JPF core on the java program given using the JPF config file that we just generated that will
  //1. Establish where the files are that need to be run.
  //2.
  exec("java -jar graph/build/RunJPF.jar ". $mainclass . ".jpf");
  //$dotPrintOut will be passed into the javascript and used on the page load so that the graph is dynamically loaded.
  $dotPrintOut = dotToString($mainclass);


 ?>

<html>
   <head>
      <title>Rough Parser</title>
      <link rel="stylesheet" type="text/css" href="style.css">
   </head>
   <body onload="checkFileAPI();">
      <script src="../src/sigma.core.js"></script>
      <script src="../src/conrad.js"></script>
      <script src="../src/utils/sigma.utils.js"></script>
      <script src="../src/utils/sigma.polyfills.js"></script>
      <script src="../src/sigma.settings.js"></script>
      <script src="../src/classes/sigma.classes.dispatcher.js"></script>
      <script src="../src/classes/sigma.classes.configurable.js"></script>
      <script src="../src/classes/sigma.classes.graph.js"></script>
      <script src="../src/classes/sigma.classes.camera.js"></script>
      <script src="../src/classes/sigma.classes.quad.js"></script>
      <script src="../src/classes/sigma.classes.edgequad.js"></script>
      <script src="../src/captors/sigma.captors.mouse.js"></script>
      <script src="../src/captors/sigma.captors.touch.js"></script>
      <script src="../src/renderers/sigma.renderers.canvas.js"></script>
      <script src="../src/renderers/sigma.renderers.webgl.js"></script>
      <script src="../src/renderers/sigma.renderers.svg.js"></script>
      <script src="../src/renderers/sigma.renderers.def.js"></script>
      <script src="../src/renderers/webgl/sigma.webgl.nodes.def.js"></script>
      <script src="../src/renderers/webgl/sigma.webgl.nodes.fast.js"></script>
      <script src="../src/renderers/webgl/sigma.webgl.edges.def.js"></script>
      <script src="../src/renderers/webgl/sigma.webgl.edges.fast.js"></script>
      <script src="../src/renderers/webgl/sigma.webgl.edges.arrow.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.labels.def.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.hovers.def.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.nodes.def.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edges.def.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edges.curve.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edges.arrow.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edges.curvedArrow.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edgehovers.def.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edgehovers.curve.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edgehovers.arrow.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.edgehovers.curvedArrow.js"></script>
      <script src="../src/renderers/canvas/sigma.canvas.extremities.def.js"></script>
      <script src="../src/renderers/svg/sigma.svg.utils.js"></script>
      <script src="../src/renderers/svg/sigma.svg.nodes.def.js"></script>
      <script src="../src/renderers/svg/sigma.svg.edges.def.js"></script>
      <script src="../src/renderers/svg/sigma.svg.edges.curve.js"></script>
      <script src="../src/renderers/svg/sigma.svg.labels.def.js"></script>
      <script src="../src/renderers/svg/sigma.svg.hovers.def.js"></script>
      <script src="../src/middlewares/sigma.middlewares.rescale.js"></script>
      <script src="../src/middlewares/sigma.middlewares.copy.js"></script>
      <script src="../src/misc/sigma.misc.animation.js"></script>
      <script src="../src/misc/sigma.misc.bindEvents.js"></script>
      <script src="../src/misc/sigma.misc.bindDOMEvents.js"></script>
      <script src="../src/misc/sigma.misc.drawHovers.js"></script>
      <script src="../plugins/sigma.renderers.customShapes/shape-library.js"></script>
      <script src="../plugins/sigma.renderers.customShapes/sigma.renderers.customShapes.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edges.dashed.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edges.dotted.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edges.parallel.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edges.tapered.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edgehovers.dashed.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edgehovers.dotted.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edgehovers.parallel.js"></script>
      <script src="../plugins/sigma.renderers.customEdgeShapes/sigma.canvas.edgehovers.tapered.js"></script>
      <div id="container">
         <div id="graphInput">
            <input type="file" onchange='readText(this)' />
         </div>
         <br/>
         <div id="parent">
            <div id="graph-container"></div>
            <div id="source">
               <div class="tab" id="tabarea">
                  <button class="tablinks" onclick="openSourceTab(event, 'Input')"id="defaultOpen">Input Source</button>
               </div>
               <div id="tabInternalsHome">
                  <div id="Input" class="tabcontent">
                     <h3>File input</h3>
                     <p>This tool will attempt to extract source files from the jar you uploaded. If you did not upload a jar or it did not include source, please upload the source files here for any files you wish to examine.<br><br> Note: Changing the settings on this pannel can have strange effects on the graph. Toggle node labels on and off to reset to the graph's default state.</p>
                     <br>
                     <input type="file" onchange='readSource(this)' /><br>
					 <input type="checkbox" name="displayLabels" id="displayLabels" onchange='toggleLabelVisibility()'  value="displayLabels">Display Node Labels<br>
  					 <input type="checkbox" name="adjustSourceLines" value="adjustSourceLines" checked id="adjustLabels" onchange=adjustLabels(false)>Adjusted Source Lines<br>
                    <input type="checkbox" name="displayThreadPositions" value="displayThreadPositions" id="displayThreadPositions" onchange=displayThreadPositions()>Use Threads for X position.<br>
                  </div>
               </div>
            </div>
         </div>
      </div>
      <script type="text/javascript">   
             var GraphDotFile = <?php echo json_encode($dotPrintOut); ?>;
         /*
                                             
         *******************************************************************************************************************************************
                                             
         Server-Frontend interface methods
                                             
         *******************************************************************************************************************************************
                                             
         */
          //This function is called when the user clicks on the load-file button to manually load a graphing file.
         function readText(filePath) {
         	document.getElementById('graphInput').style.visibility = "hidden";
         	document.getElementById("graphInput").style.display = "none";
         	var output = "";
         	if (filePath.files && filePath.files[0]) {
         		reader.onload = function (e) {
         			output = e.target.result;
         			initGraph(output);
         		};
         		reader.readAsText(filePath.files[0]);
         	} else {
         		alert('Unable to access local files due to browser security settings. ' +
         			'To overcome this, go to Tools->Internet Options->Security->Custom Level. ' +
         			'Find the setting for "Initialize and script ActiveX controls not marked as safe" and change it to "Enable" or "Prompt"');
         		return false;
         	}
         	return output;
         }
          
         //This is where we try to load the file, if we can. Right now the server doesn't exist.
         function loadFile(fileName) {
         	return false;
         }
          
          //If this method succeeds, it will hide manual loading options.
         function attemptGraphLoadFromServer(dotString){
             
             //The raw text file of the graph goes here.
             var graphContents = dotString;
             if(graphContents!=''){
                document.getElementById('graphInput').style.visibility = "hidden";
                document.getElementById("graphInput").style.display = "none";
                initGraph(graphContents);
             }
         }
         /*
         
         *******************************************************************************************************************************************
         
         Frontend Methods
         
         *******************************************************************************************************************************************
         
         */
         sigma.utils.pkg('sigma.canvas.nodes');
         document.getElementById("defaultOpen").click();
         document.getElementById('graph-container').style.visibility = "hidden";
		 var newLabelMode = true;
		  
		  function toggleLabelVisibility(){
			  var visible =  document.getElementById("displayLabels").checked;
			  shouldDrawLabels = visible;
              if(!newLabelMode){
                  document.getElementById("adjustLabels").checked = true;
                  adjustLabels(true);
              }
			  s = makeGraph(rawGraph);

              s.refresh();
		  }
          
          function displayThreadPositions(){
              var useThreads = document.getElementById("displayThreadPositions").checked;
              var toSet;
              var trueNodes = s.graph.nodes();
                  for(var i=0;i<trueNodes.length;i++){
                      if(useThreads){
                          toSet= nodes[i].threadWidth;
                      }
                      else{
                          toSet=nodes[i].arbitraryWidth;
                          
                      }
                      if(Number.isInteger(toSet)){
                          trueNodes[i].x = toSet;
                      }
                      else{
                          console.log(toSet+" is not an integer!"+" node "+nodes[i].label+" cannot be set.");
                          //trueNodes[i].x =0;
                      }
                  }
                  s.refresh();
          }
		  
		  function adjustLabels(forced){
			  var adjusted =  document.getElementById("adjustLabels").checked;
              var nodes2;
			  //We've made a change to the mode and need to draw again.
			  if(newLabelMode!=adjusted||forced){
				  newLabelMode = adjusted;
                  nodes2 = s.graph.nodes();
                  for(var i=0;i<nodes2.length;i++){
                      var tempLabel = nodes2[i].label;
                      nodes2[i].label=oldLabels[i];
                      oldLabels[i] = tempLabel;
                  }
                  s.refresh();
                  
			  }
		  }

         function openSourceTab(evt, sourcePageName) {
         	var i, tabcontent, tablinks;
         	tabcontent = document.getElementsByClassName("tabcontent");
         	for (i = 0; i < tabcontent.length; i++) {
         		tabcontent[i].style.display = "none";
         	}
         	tablinks = document.getElementsByClassName("tablinks");
         	for (i = 0; i < tablinks.length; i++) {
         		tablinks[i].className = tablinks[i].className.replace(" active", "");
         	}
         	document.getElementById(sourcePageName).style.display = "block";
         	evt.currentTarget.className += " active";
         }

         function readSource(filePath) {
         	var output = "";
         	if (filePath.files && filePath.files[0]) {
         		var sourceName = filePath.files[0].name;
         		loadedSource += ' ' + filePath.files[0].name;
         		console.log('loaded source: ' + sourceName);
         		reader.onload = function (e) {
         			output = e.target.result;
         			var tabName = "tab" + sourceName;
         			document.getElementById("tabarea").innerHTML += "<button id= \"" + tabName + "\"class=\"tablinks\" onclick=\"openSourceTab(event, '" + sourceName + "')\">" + sourceName + "</button>";
         			document.getElementById("tabInternalsHome").innerHTML += buildNewTab(sourceName);
         			parseFile(output, sourceName);
         			document.getElementById(tabName).click();
         		};
         		reader.readAsText(filePath.files[0]);
         	} else if (ActiveXObject && filePath) {
         		alert('Unable to access local files due to browser security settings. ' +
         			'To overcome this, go to Tools->Internet Options->Security->Custom Level. ' +
         			'Find the setting for "Initialize and script ActiveX controls not marked as safe" and change it to "Enable" or "Prompt"');
         	} else {
         		return false;
         	}
         	return output;
         }

         function buildNewTab(sourceName) {
         	var output = "";
         	output += "<div id=\"" + sourceName + "\" class=\"tabcontent\">";
         	output += "<div id=\"codebox\"><code id=\"text_spot" + sourceName + "\"></code></div>";
         	output += "</div>";
         	return output;
         }
         /**
          * Check for the various File API support.
          */
         function checkFileAPI(dotString) {
         	if (window.File && window.FileReader && window.FileList && window.Blob) {
         		reader = new FileReader();
                attemptGraphLoadFromServer(GraphDotFile);
         		return true;
         	} else {
         		return false;
         	}
         }
         //Parses input file into the source display.
         function parseFile(input, sourceName) {
         	input = "\n" + input;
         	var output = "<div class='code_line'><pre>" + input.replace(/\n/g, "</pre></div><div class='code_line' id=\"code_line_" + sourceName + "\"><pre>") + "</pre></div>";
         	var textLocation = "text_spot" + sourceName;
         	document.getElementById(textLocation).innerHTML = output;
         	var lines = document.getElementsByClassName('code_line');
         	var importantLines = [];
         	for (var i = 0; i < lines.length; i++) {
         		var line = lines[i];
         		var id = line.id;
         		if (id.includes(sourceName)) {
         			importantLines.push(line);
         		}
         	}
         	for (var i = 0; i < importantLines.length; i++) {
         		var line = importantLines[i];
         		var html = line.innerHTML;
         		line.id = sourceName + "_line" + (i + 1);
         		line.innerHTML = html.replace("<pre>", "<pre style='margin: 0px'>  " + (i + 1) + ". ");
         	}
         	return 1;
         }

         function jumpToLine(lineToJump, sourceName) {
         	var lineId = sourceName + '_line' + lineToJump;
         	lineToJump = lineToJump - 1;
         	var lines = document.getElementsByClassName('code_line');
         	var importantLines = [];
         	for (var i = 0; i < lines.length; i++) {
         		var line = lines[i];
         		var id = line.id;
         		if (id.includes(sourceName)) {
         			line.style.backgroundColor = '#deeff5';
         			importantLines.push(line);
         		}
         	}
         	var line = importantLines[lineToJump];
         	line.style.backgroundColor = "pink";
         	document.getElementById('codebox').scrollTop = document.getElementById(lineId).offsetTop - document.getElementById('codebox').offsetTop;
         	//now change back the last line.
         	lastLine = lineToJump;
         }

         function loadSourceLine(sourceLine,rightClicked) {
             var adjusted =  document.getElementById("adjustLabels").checked;
             var label = sourceLine;
             
             //We now need to know if there will be multiple sections in the source we're given.
             if(adjusted){
                 var loc = sourceLine.search("--");
                 if(loc != -1){
                    if(rightClicked){
                     label = sourceLine.substr(loc+2,sourceLine.length);
                     }
                     else{
                         label = sourceLine.substr(0,loc);
                     }
                 }
             }

             console.log("Loading line: "+label);
             label = label.split(':');
             console.log("Split line: "+label);
             
         	if (!(label.length > 1)) {
         		return;
         	}
         	var newLabelName = label[0];
         	//We have the right file loaded, we can jump directly to the line we want.
         	if (loadedSource.includes(newLabelName)) {
         		var tabName = "tab" + newLabelName + ".java";
         		document.getElementById(tabName).click();
         		jumpToLine(label[label.length - 1], newLabelName + ".java");
         	}
         	//We don't have the right class loaded.
         	else {
         		var worked = loadFile(newLabelName);
         		if (worked) {
         			var tabName = "tab" + newLabelName + ".java";
         			document.getElementById(tabName).click();
         			jumpToLine(label[label.length - 1], newLabelName + ".java");
         		} else {
         			alert('Server could not extract source. Please upload source file.');
         		}
         	}
         }
         /*
         
         *******************************************************************************************************************************************
         
         Graphing Methods
         
         *******************************************************************************************************************************************
         
         */
         var widths = {};
         var colors = {};
         var horizontal_spacing = 50;
         var vertical_spacing = 40;
         var edgesCreated = 0;
         var nodesCreated = 0;
         var numThreads = {};
         var childless = [];
         var orphans = [];
         var edges = [];
         var nodes = [];
         var numNodes = 0;
         var lastLine = 0;
         var closures = [];
          var finishNodes =[];
         var loadedSource = "";
		 var lastLabel = "";
		 var shouldDrawLabels = false;
		 var rawGraph;
		 var oldLabels; 
          var numFinishBlocks =2;
		 var s;
		  
		  
         function initGraph(input) {
         	var tokens = input.split(/\s+/);
         	var out = '  ';
         	for (var i = 0; i < tokens.length; i++) {
         		out += tokens[i] + '<br />';
         	}
         	rawGraph = JSON.parse(parseGraph(tokens));
         	document.getElementById('graph-container').style.visibility = "visible";
         	makeGraph(rawGraph);
         }
		  function makeGraph(graphInput){
			  if(s!=null){
				  s.kill();
			  }
			  s = new sigma({
         		graph: graphInput,
         		renderer: {
         			container: 'graph-container',
         			type: 'canvas'
         		},
         		settings: {
         			minNodeSize: 1,
         			maxNodeSize: 10,
         			minEdgeSize: 0.1,
         			maxEdgeSize: 5,
         			enableEdgeHovering: true,
         			edgeHoverSizeRatio: 3,
         			doubleClickEnabled: false,
         			edgeHoverColor: 'edge',
         			defaultEdgeHoverColor: '#000',
         			edgeHoverSizeRatio: 2,
					drawLabels: shouldDrawLabels,
         			edgeHoverExtremities: true
         		}
         	});
         	s.bind('clickNode ', function (e) {
                if(e.data.node.label!=""){
                    console.log("left click"+e.data.node.label);
                    loadSourceLine(e.data.node.label, false);
                }
         	});
            s.bind('rightClickNode doubleClickNode', function (e) {
                if(e.data.node.label!=""){
                    console.log("right click"+e.data.node.label);
                    loadSourceLine(e.data.node.label, true);
                }
         		
         	});
         	CustomShapes.init(s);
         	s.refresh();
            return s;
		  }
		            
        function getBetterThreadNames(root) {
            
         	if (root['children'].length == 1) {
         		var childNode = root['children'][0];
         		getBetterThreadNames(childNode);
         	} else if (root['children'].length == 2) {
         		var childNodeLeft = root['children'][0];
         		var childNodeRight = root['children'][1];
         		getBetterThreadNames(childNodeLeft);
         		getBetterThreadNames(childNodeRight);
         	}
            if(root.modified==false){
                root.label= getAdjustedLabel(root);
            }
            
         }
		  
        function getAdjustedLabel(node){
			  var parent = node.parent;
			  var output = node.label;
            var containsFinish = node.label.search("Habanero Finish");
			  //We only want to continue if the node has any parent. The root can't be renamed.
			  if(parent!=null&&node['type']=="circle"&&parent['type']=="diamond"&&containsFinish==-1){
				  output ="";
                  node.modified=true;
                  if(parent.modified ==false){
                      parent.label = parent.label+"--"+node.label;
                      parent.modified = true;
                  }
				  
			  }
			  
			  return output;
		  }


         function parseGraph(tokens) {
         	sigma.utils.pkg('sigma.canvas.nodes');
         	var choiceGeneratorNodeSize = 4;
         	var otherNodeSize = 2;
         	var jsonOutput = {
         		nodes: [],
         		edges: []
         	};
         	//Main loop through the tokens.
         	var index = 0;
         	var state = 0;
         	var firstNumber = '';
         	var secondNumber = '';
         	var out = '';
         	widths.length = 0;
         	numThreads.length = -1;
         	buildColors();
         	while (index < tokens.length) {
         		var currentToken = tokens[index];
         		if (currentToken == '' || currentToken == '\n') {
         			index++;
         			continue;
         		}
         		switch (state) {
         		case 0:
         			// header
         			if (currentToken == 'digraph') {
         				state++;
         			} else {
         				state = 99;
         			}
         			break;
         		case 1:
         			// name
         			// We don't care about it
         			state++;
         			break;
         		case 2:
         			// {
         			if (currentToken == '{') {
         				state++;
         			} else {
         				state = 99;
         			}
         			break
         		case 3:
         			// First number or end
         			if (currentToken == '}') {
         				state = 98;
         			} else {
         				firstNumber = currentToken;
         				state++;
         			}
         			break;
         		case 4:
         			if (currentToken == '->') {
         				// Edge start
         				state = 6;
         			} else if (currentToken == '[') {
         				// Node start
         				state = 5;
         			}
         			break;
         		case 5:
         			if (currentToken == '];') {
         				// End node;
         				state = 3;
         			} else {
         				// parse LABEL;
         				var c = currentToken.split('=');
         				if (c[0] != 'label') {
         					state = 99;
         				} else {
         					var value = c[1];
         					if (value.substr(0, 1) == '"' && value.substr(value.length - 1) == '"') {
         						nodesCreated++;
         						value = value.substr(1, value.length - 2);
         						value = getLabel(value);
         						var tempNode = {
         							id: firstNumber,
         							label: value,
         							x: 0,
         							y: 0,
         							size: 5,
         							children: [],
         							parent: null
         						};
         						
								//We don't want the last node, ever.
         						if (nodesCreated != 3) {
									
         							nodes.push(tempNode);
         							numNodes++;
         						}
         					} else {
         						state = 99;
         					}
         				}
         			}
         			break;
         		case 6:
         			// Second edge number
         			secondNumber = currentToken;
         			state++;
         			break;
         		case 7:
         			if (currentToken == '[') {
         				state++;
         			} else {
         				state = 99;
         			}
         			break;
         		case 8:
         			if (currentToken == '];') {
         				// End edge;
         				state = 3;
         			} else {
         				// parse COLOR;
         				var c = currentToken.split('=');
         				if (c[0] != 'color') {
         					state = 99;
         				} else {
         					var value = c[1];
         					if (value.substr(0, 1) == '"' && value.substr(value.length - 1) == '"') {
         						value = value.substr(1, value.length - 2);
         						var tempEdge = {
         							id: "" + edgesCreated,
         							source: firstNumber,
         							target: secondNumber,
         							color: value
         						};
         						tempEdge['color'] = colors[value];
         						if (tempEdge.target != 3) {
         							edges.push(tempEdge);
         							edgesCreated++;
         						}
         					} else {
         						state = 99;
         					}
         				}
         			}
         			break;
         		default:
                    return null;
         			break;
         		}
         		index++;
         	}
         	for (var key in edges) {
         		var edge = edges[key];
         		var sourceNode = findNode(edge['source'], nodes);
         		var targetNode = findNode(edge['target'], nodes);
         		if (sourceNode != null) {
         			sourceNode['children'].push(targetNode);
         			targetNode['parent'] = sourceNode;
         		}
         		if (getColor(edge) == colors['green']) {
         			edge.type = "parallel";
         			edge.size = 4;
         		}
         	}
         	var rootNode = null;
         	for (var key in nodes) {
         		var node = nodes[key];
         		if (node['parent'] == null) rootNode = node;
         	}
         	setDepth(rootNode, 10);
         	getNumThreads(rootNode);
         	setWidth(rootNode);
         	//Add additional nodes. 
         	findChildless(rootNode);
         	giveFalseChildren();
         	findFinishClosures(rootNode);
         	giveFalseClosures();
         	//Assign types to each node
         	assignTypes(rootNode);
            oldLabels = [];
            for(var i =0;i< nodes.length;i++){
                nodes[i].label=getNodeLabel(nodes[i]['label']);
                nodes[i].modified = false;
                oldLabels.push(nodes[i].label);
            }
            getBetterThreadNames(rootNode);
             moveFinishNodes();
         	//Make final JSON for display.
         	var jsonOutput = '{ "nodes": [';
         	for (var i = 0; i < nodes.length; i++) {
         		var node = nodes[i];
         		var temp_label = node['label'];
         		//temp_label = getNodeLabel(node['label']);
         		jsonOutput = jsonOutput + '{"id": "' + node['id'] + '","type": "' + node['type'] +'","threadWidth": "' + node['threadWidth'] +'","arbitraryWidth": "' + node['arbitraryWidth'] + '","label": "' + temp_label + '","color": "' + node['color'] + '","borderColor": "' + node['borderColor'] + '","x": ' + node['x'] + ',"y": ' + node['y'] + ',"size": ' + node['size'] + '}';
         		if (i + 1 < nodes.length) jsonOutput = jsonOutput + ',';
         	}
         	jsonOutput = jsonOutput + '], "edges": [';
         	for (var i = 0; i < edges.length; i++) {
         		var edge = edges[i];
         		jsonOutput = jsonOutput + '{"id": "' + edge['id'] + '","source": "' + edge['source'] + '","label": "' + edge['label'] + '","type": "' + edge['type'] + '","target": "' + edge['target'] + '","color": "' + getColor(edge) + '"}';
         		if (i + 1 < edges.length) jsonOutput = jsonOutput + ',';
         	}
         	jsonOutput = jsonOutput + ']}';
         	//console.log(jsonOutput);
         	return jsonOutput;
         }
		 
		  //The goal here is to make the graph more readable. To do this we will remove the labels
		  //from nodes that don't convey useful information.
		 function testForNewLabel(node){
			 var output = node.label;
			 var code1="";
			 var code2="";
			 
			 if(node.label.search("SuspendableActivity")!=-1&&lastLabel.search("Activity")){
				 //It is possible we want to change the current label. We need to check if they're pointing to the same location in code.
				 code1=node.label.substr(node.label.search("\\+"),node.label.length-1);
				 code2=lastLabel.substr(lastLabel.search("\\+"),lastLabel.length-1);
				 if(code1===code2){
					 output=node.label.substr(0,node.label.search("\\+")-1)+"Thread Continues.java:0";
					 console.log("changed label to: "+output);
				 }
				 else{
					 console.log("Nodes: "+lastLabel+" and "+node.label+" are not equal. No change to make.");
				 }
				 
			 }
				 
			 return output;
		 }
          
          function moveFinishNodes(){
              for(var key in nodes){
                  var node = nodes[key];
                  if (node.type == 'square'){
                      finishNodes.push(node);
                  }
              }
              
              //Now we know all of our finish nodes.
              for (var key in finishNodes){
                  var node = finishNodes[key];
                  node.finishedSwitched = true;
                  
                  for (var innerKey in finishNodes){
                      var innerNode = finishNodes[innerKey];
                      if(innerKey!=key&& node.label ==innerNode.label){
                          //We need a new edge between these two nodes and to remove all previous edges.
                          
                          if(!innerNode.finishedSwitched){
                              makeEdge(node,innerNode,'green','solid','Finish Block');
                              innerNode.finishedSwitched= true;
                              node.x = node.x -(horizontal_spacing* numFinishBlocks++)/2;
                              node.label = 'Start Finish';
                              innerNode.label= 'End Finish';
                              innerNode.x = node.x;
                          }
                          break;
                          
                      }
                      
                    }
              }
          }
          
          function makeEdge(sourceNode,destNode,edgeColor,edgeType,edgeLabel){
             var tempEdge = {
         		id: "" + edgesCreated,
         		source: sourceNode.id,
         		target: destNode.id,
         		color: colors[edgeColor],
                 label: edgeLabel
         	};
         	tempEdge.type = edgeType;
         	edges.push(tempEdge);
         	edgesCreated++; 
          }
          
         function findNode(id, nodes) {
         	for (var key in nodes) {
         		var node = nodes[key];
         		if (node['id'] == id) return node;
         	}
         	return null;
         }

         function findEdge(source, target) {
         	for (var key in edges) {
         		var edge = edges[key];
         		var sourceNode = findNode(edge['source'], nodes);
         		var targetNode = findNode(edge['target'], nodes);
         		if (sourceNode.id == source.id && target.id == targetNode.id) {
         			return edge;
         		}
         	}
         	return null;
         }

         function setDepth(root, level) {
         	if (root['y'] < level) root['y'] = level;
         	for (var child in root['children']) {
         		setDepth(root['children'][child], level + vertical_spacing);
         	}
         }

         function findChildless(root) {
         	if (root['children'].length == 1) {
         		var childNode = root['children'][0];
         		findChildless(childNode);
         	} else if (root['children'].length == 2) {
         		var childNodeLeft = root['children'][0];
         		var childNodeRight = root['children'][1];
         		findChildless(childNodeLeft);
         		findChildless(childNodeRight);
         	} else {
         		//Found a node with no children, no need to go deeper.
         		childless.push(root);
         	}
         }

         function buildColors() {
         	colors['blue'] = "#0000FF";
         	colors['green'] = "#008000";
         	colors['red'] = "#800000";
         	colors['purple'] = "#DECAEE";
         	colors['pink'] = "#FF69B4";
         	colors['black'] = "#000000";
         	colors['yellow'] = "#000000";
         }
         //Quickly traverses the entire graph and finds out how many threads we have.
         function getNumThreads(root) {
         	root['x'] = explore(root);
         	if (root['children'].length == 1) {
         		var childNode = root['children'][0];
         		getNumThreads(childNode);
         	} else if (root['children'].length == 2) {
         		var childNodeLeft = root['children'][0];
         		var childNodeRight = root['children'][1];
         		getNumThreads(childNodeLeft);
         		getNumThreads(childNodeRight);
         	}
         	numThreads.spacing = (numThreads.length) * horizontal_spacing;
         }

         function giveFalseChildren() {
         	/*Method must:
         	1. Create new child node for parent.
         	2. Add new child as child of real parent.
         	3. Add parent as parent of new child.
         	4. Add implicit edge to parent.
         	5. If there are any other false children, add an edge to them.
         	*/
         	//First we need to know the depth of these new nodes.
         	var maxDepth = 0;
         	var numOrphans = childless.length;
         	for (var i = 0; i < numOrphans; i++) {
         		var tempNode = childless[i];
         		if (tempNode['y'] > maxDepth) {
         			maxDepth = tempNode['y'];
         		}
         	}
         	maxDepth += 2 * vertical_spacing;
         	for (var childlessIndex = 0; childlessIndex < numOrphans; childlessIndex++) {
         		var parent = childless[childlessIndex];
         		makeKid(maxDepth, parent);
         	}
         }

         function makeKid(maxDepth, tempNode) {
         	var newChild = {
         		id: (nodes.length + 2),
         		label: tempNode['label'] + 'end',
         		x: 0,
         		y: maxDepth,
         		size: 1,
         		children: [],
         		parent: null
         	};
         	tempNode['children'].push(newChild);
         	newChild['parent'] = tempNode;
         	setWidth(newChild);
         	newChild['label'] = 'Habanero Implicit Finish';
         	nodes.push(newChild);
         	var tempEdge = {
         		id: "" + edgesCreated,
         		source: tempNode['id'],
         		target: newChild['id'],
         		color: colors['purple']
         	};
         	tempEdge.type = 'dashed';
         	edges.push(tempEdge);
         	edgesCreated++;
         	if (orphans.length > 0) {
         		numOrphans = orphans.length;
         		for (var j = 0; j < numOrphans; j++) {
         			var sibling = orphans[j];
         			var tempEdge = {
         				id: "" + edgesCreated,
         				source: newChild['id'],
         				target: sibling['id'],
         				color: colors['purple']
         			};
         			tempEdge.type = 'dashed';
         			edges.push(tempEdge);
         			edgesCreated++;
         		}
         	}
         	orphans.push(newChild);
         }
         //Utility function of getNumThreads that does the actual exploring.
         function explore(node) {
         	var nodeLabel = node.label;
         	var pos = nodeLabel.search('T:');
         	var endThreadID = pos;
         	var advance = true;
         	//First, find the thread ID.
         	while (advance) {
         		if (nodeLabel.substring(endThreadID, endThreadID + 1).search('\\+') !== -1 || nodeLabel.substring(endThreadID, endThreadID + 1).search('\\-') !== -1) {
         			advance = false;
         		} else {
         			endThreadID++;
         		}
         	}
         	var threadID = nodeLabel.substring(pos, endThreadID);
         	if (numThreads.length == 0) {
         		numThreads[threadID] = 0;
         		numThreads.length++;
         		return 0;
         	}
         	//Next, if we're not at the root, and our thread has been seen before.
         	else if (numThreads[threadID] != null) {
         		return numThreads[threadID];
         	}
         	//Last, if we've never seen this thread before.
         	else {
         		numThreads[threadID] = 0;
         		if (nodeLabel.search('FinishScope') == -1) {
         			numThreads.length++;
         		}
         		return numThreads[threadID];
         	}
         }

         function assignTypes(root) {
         	root['type'] = getType(root);
         	if (root['children'].length == 1) {
         		var childNode = root['children'][0];
         		assignTypes(childNode);
         	} else if (root['children'].length == 2) {
         		var childNodeLeft = root['children'][0];
         		var childNodeRight = root['children'][1];
         		assignTypes(childNodeLeft);
         		assignTypes(childNodeRight);
         	}
         }

         function getType(root) {
         	var label = root.label;
         	if (label.search('SuspendableActivity') != -1) {
         		root.color = colors['yellow'];
         		root.borderColor = colors['yellow'];
         		root.size = 4;
         		return "diamond";
         	} else if (label.search('FinishScope') != -1) {
         		root.size = 4;
         		if (label.search('-end') != -1) {
         			root.color = colors['red'];
         			root.borderColor = colors['red'];
                    
         		} else {
         			root.color = colors['green'];
         			root.borderColor = colors['green'];
         		}
               // root.x = root.x - 50;
         		return "square";
         	} else if (label.search('Habanero Finish') != -1) {
         		root.color = colors['red'];
         		root.borderColor = colors['red'];
                
         		return "circle";
         	} else if (label.search('Activity') != -1) {
         		root.size = 4;
         	}
         	root.color = colors['black'];
         	root.borderColor = colors['black'];
         	return "circle";
         }
         //Determine if the node we're looking at is the last node before the end of a finish. If it is, add it to the list of finish closure nodes.
         function findFinishClosures(root) {
         	if (root['children'].length == 1) {
         		var childNode = root['children'][0];
         		var isFinish = childNode.label.search('FinishScope');
         		var isClosure = childNode.label.search('-end');
         		if (isFinish != -1 && isClosure != -1) {
         			//We've found a finish closure.
         			closures.push(root);
         		}
         		findFinishClosures(childNode);
         	} else if (root['children'].length == 2) {
         		var childNodeLeft = root['children'][0];
         		var childNodeRight = root['children'][1];
         		findFinishClosures(childNodeLeft);
         		findFinishClosures(childNodeRight);
         	}
         }
         /*Iterate through each finish closure and do the following: 
         1. Remove edge to finish closure node.
         2. Create new node on same level as finish closure node.
         3. Connect new node to finish closure node.
         4. Connect new node to this node.
         */
         function giveFalseClosures() {
         	for (var i = 0; i < closures.length; i++) {
         		var tempNode = closures[i];
         		var finishClosingNode = tempNode.children[0];
         		//First, creates new node for the finish closure.
         		var newChild = {
         			id: (nodes.length + 2),
         			label: 'Habanero Finish',
         			x: 0,
         			y: 0,
         			size: 1,
         			children: [],
         			parent: null
         		};
         		newChild['parent'] = tempNode;
         		tempNode['children'][0] = newChild;
         		newChild.children.push(finishClosingNode);
         		newChild.x = tempNode.x;
         		newChild.y = finishClosingNode.y;
         		nodes.push(newChild);
         		//Now, find the edge that used to point
         		var tempEdge = findEdge(tempNode, finishClosingNode);
         		tempEdge.target = newChild.id;
         		if (tempEdge.color != colors['black']) {
         			tempEdge.type = "dotted";
         		}
         		tempEdge = {
         			id: "" + edgesCreated,
         			source: newChild['id'],
         			target: finishClosingNode['id'],
         			color: colors['red']
         		};
         		tempEdge.type = "dotted";
         		edges.push(tempEdge);
         		edgesCreated++;
         	}
         }

         function setWidth(root) {
         	//First set position of current node.
         	root['x'] = getWidth(root,false);
             root.arbitraryWidth = root['x'];
         	if (root['children'].length == 1) {
         		var childNode = root['children'][0];
         		setWidth(childNode);
         	} else if (root['children'].length == 2) {
         		var childNodeLeft = root['children'][0];
         		var childNodeRight = root['children'][1];
         		setWidth(childNodeLeft);
         		setWidth(childNodeRight);
         	}
         }
         //returns the last x coordinate of a parent that happened to be a finish.
         function getLastFinishX(node) {
         	var parent = node['parent'];
         	if (parent == null) {
         		return node['x'];
         	}
         	var isFinish = parent.label.search('FinishScope');
         	var isEnd = parent.label.search('-end');
         	if (isFinish != -1 && isEnd == -1) {
         		return parent.x;
         	}
         	return getLastFinishX(parent);
         }

         function getWidth(node,arbitrary) {
         	var nodeLabel = node.label;
         	var pos = nodeLabel.search('T:');
         	//If this node doesn't have a thread ID, use the parent's thread width instead.
         	if (pos == -1 && widths.length != 0&& node['parent'] != null) {
         		return node['parent']['x'];
         	}
         	var pos2 = nodeLabel.search('FinishScope');
         	if (pos2 != -1 && node['parent'] != null) {
         		var pos3 = nodeLabel.search('-end');
         		//We have the start of a finish scope, it's safe to use our parent's x position.
         		if (pos3 == -1&&!arbitrary) {
         			return node['parent']['x'];
         		} else if (!arbitrary){
         			return getLastFinishX(node);
         		}
         	}
         	var endThreadID = pos;
         	var advance = true;
         	//First, find the thread ID.
         	while (advance) {
         		if (nodeLabel.substring(endThreadID, endThreadID + 1).search('\\+') !== -1 || nodeLabel.substring(endThreadID, endThreadID + 1).search('\\-') !== -1) {
         			advance = false;
         		} else {
         			endThreadID++;
         		}
         	}
         	var threadID = nodeLabel.substring(pos, endThreadID);
            
             node.threadWidth = threadID.split(':')[1] * horizontal_spacing;
             if(!Number.isInteger(node.threadWidth)){
                 getWidth(node.parent);
                 node.threadWidth = node.parent.threadWidth;
                 console.log("Threadwidth on broken node with thread ID: "+threadID+" set to: "+node.threadWidth);
             }
             
             if(arbitrary){
                 return threadWidth;
             }

         	//Now that we have it, we compare against our chart.
         	//First, if we're at the root node, it is our first thread.
         	if (widths.length == 0) {
         		widths.length++;
         		widths[threadID] = 0;
         		widths.back = threadID;
         		widths.root = threadID;
         		node.label = "Habanero Implicit Finish";
         		node.size = 1;
         		return 0;
         	}
         	//Next, if we're not at the root, and our thread has been seen before.
         	else if (widths[threadID] != null) {
         		return widths[threadID];
         	}
         	//Last, if we've never seen this thread before.
         	else {
         		//Special case: our enclosing thread should be displayed in line with the root node because reasons.  
         		if (widths.length == 1) {
         			widths[threadID] = 0;
         		} else {
         			numThreads.spacing = numThreads.spacing - horizontal_spacing;
         			var newWidth = numThreads.spacing;
         			widths[threadID] = newWidth;
         		}
         		widths.length++;
         		widths.back = threadID;
         		return widths[threadID];
         	}
         }

         function getDepth(root) {
         	var out = root['x'] + " " + root['y'] + "<br />";
         	for (var child in root['children']) {
         		out = out + getDepth(root['children'][child]);
         	}
         	return out;
         }

         function getNodeLabel(oldLabel) {
         	var output = "";
			 var code1="";
			 var code2="";
         	//Now, we'll find where the thread label starts.
         	var nodeLabel = oldLabel;
         	var pattern = '\\+';
         	var reg = new RegExp(pattern, '');
         	var pos = nodeLabel.search(reg);
         	if (pos == -1) {
         		output = oldLabel;
         	}

			 else if(oldLabel.search("SuspendableActivity")!=-1&&lastLabel.search("Activity")){
				 if(oldLabel.search("\\+")!=-1&&lastLabel.search("\\+")!=-1){
				 //It is possible we want to change the current label. We need to check if they're pointing to the same location in code.
				 code1=oldLabel.substr(oldLabel.search("\\+"),oldLabel.length-1);
				 code2=lastLabel.substr(lastLabel.search("\\+"),lastLabel.length-1);
					 if(code1===code2){
						 output=oldLabel.substr(0,oldLabel.search("\\+")-1)+"Thread Continues.java:0";
						 console.log("changed label to: "+output);
					 }
					 else {
						output = getGenericLabel(oldLabel);
					}
				}
				else{
					output = getGenericLabel(oldLabel);
				}
				 
			 } 
			 
			 else {
         		output = getGenericLabel(oldLabel);
         	 }
		  
		  lastLabel=output;
         	return output;
         }
		  function threadContinues(oldLabel){
			  return '';
			  //return getGenericLabel(oldLabel);
		  }
		  function getGenericLabel(oldLabel){
			  	var nodeLabel = oldLabel;
         	var pattern = '\\+';
         	var reg = new RegExp(pattern, '');
         	var pos = nodeLabel.search(reg);
         		var output = nodeLabel.substring((pos + 1), nodeLabel.length);
			  
         		pattern = 'java';
         		reg = new RegExp(pattern, '');
         		pos = output.search(pattern);
         		var beforeJava = output.substring(0, pos - 1);
         		var afterJava = output.substring((pos + 4), output.length);
         		output = beforeJava + afterJava;
			  return output;
         	
		  }
         function getColor(edge) {
         	if (edge.color != undefined) {
         		return edge.color;
         	}
         	return colors.black;
         }

         function displayContents(txt) {
         	console.log(txt);
         }

         function getLabel(value) {
         	return value;
         }

      </script>
   </body>
</html>