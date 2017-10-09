






<html>

<head>
    <title>Bootstrap 101 Template</title>
    <link href="index.css" rel="stylesheet">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">
</head>

<body>
    <div id="page">
        <h2>
            Java Habanero Analytics
        </h2>

        <form id="get-file" class="container-fluid" action="<?echo "WebParser/server/Frontend.php" ?>" method="POST"  enctype="multipart/form-data">
            <div class="container-fluid">
                <div class="padding-top-10">
                  <input type="radio" name="gender" value="file" onchange="displaySwitch(true);" checked>Input File<br>
                </div>
                <div class="padding-top-10">
                  <input type="radio" name="gender" value="source-code" onchange="displaySwitch(false)">Paste Source Code<br>
                </div>
                <div class="row padding-top-25" >
                  <div class="col">
                      <h3>Name of Main Class</h3>
                      </br>
                      <input id="input-text" type="text" name="mainclass">
                  </div>
                    <div class="col" id="input-file">
                        <h3>Insert Jar File</h3>
                        </br>
                        <input id="uploadedJarFile" type="file" name="upload">
                    </div>

                </div>
                <div class="row padding-10">
                    <textarea form="get-file" id="input-text-area" type="textarea" name="javacode">Place your java code here. Debug before placing.</textarea>
                </div>
                <div class="row">
                    <input type="submit" name="sub" value="Submit ">
                </div>
            </div>
        </form>
        <div class="container-fluid">

        </div>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js "></script>
        <!-- Include all compiled plugins (below), or include individual files as needed -->
        <script>
            function displaySwitch(code) {
                var turnON;
                var turnOFF;
                //switches the correct item on and off
                if (code) {
                    turnON = 'input-file';
                    turnOFF = 'input-text-area';
                } else {
                    turnOFF = 'input-file';
                    turnON = 'input-text-area';
                }
                document.getElementById(turnOFF).style.display = 'none';
                document.getElementById(turnON).style.display = 'inline';

            }
        </script>
</body>

</html>
