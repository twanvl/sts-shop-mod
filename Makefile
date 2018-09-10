
all:
	make -C src/main/resources/img/relics
	mvn package

clean:
	mvn clean
	make clean -C src/main/resources/img/relics
