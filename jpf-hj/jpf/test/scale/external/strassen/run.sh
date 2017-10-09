javac *.java
hjc -rt c   Main.hj
echo "Strassen parallel execution"
hj   -J-Xmx60g -places 1:16 Main
