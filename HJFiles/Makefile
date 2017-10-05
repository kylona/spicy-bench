NAME=Antidep1VarYes
FILE=${NAME}.java
LOOPS=10


all:
	javac -cp classes:classes/hj-lib-byu.jar -d classes ${FILE}

run:
	java -cp classes:classes/hj-lib-byu.jar ${NAME}

loop:
	while [ true ] ; do \
	 java -cp classes:classes/hj-lib-byu.jar ${NAME} ; \
	done

clean:
	rm ${NAME}.class
