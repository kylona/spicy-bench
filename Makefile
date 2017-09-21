NAME=Antidep2VarYes
FILE=${NAME}.hj

all:
	hjc -d classes -rt s ${FILE}

jav:
	cp ${FILE} ${NAME}.java
	javac -d . ${NAME}.java

run:
	hj -cp classes Antidep2VarYes.hj

clean:
	rm ${NAME}.java ${NAME}.class
