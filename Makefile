NAME=Antidep1OrigYes
FILE=${NAME}.hj

all:
	hjc -d classes -rt s ${FILE}

jav:
	cp ${FILE} ${NAME}.java
	javac -d . ${NAME}.java

run:
	hj -cp classes ${FILE}

clean:
	rm ${NAME}.java ${NAME}.class
