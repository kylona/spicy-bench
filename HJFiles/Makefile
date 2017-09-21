NAME=Antidep1VarYes
FILE=${NAME}.hj
LOOPS=10


all:
	hjc -d classes -rt s ${FILE}

jav:
	cp ${FILE} ${NAME}.java
	javac -d . ${NAME}.java

run:
	hj -cp classes ${FILE}

loop:
	while [ true ] ; do \
	hj -cp classes  ${FILE} ; \
	done

clean:
	rm ${NAME}.java ${NAME}.class
