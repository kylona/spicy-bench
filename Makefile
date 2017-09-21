NAME=DoAll1OrigNo
FILE=${NAME}.hj

all:
	hjc -d classes -rt s ${FILE}

jav:
	cp ${FILE} ${NAME}.java
	javac -d . ${NAME}.java
