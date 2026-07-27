.PHONY: all build package test clean lint run-cli run-ui docker-build help

all: build

help:
	@echo "Available targets:"
	@echo "  make build        - Compile the project (mvn compile)"
	@echo "  make package      - Package the project into a JAR (mvn package)"
	@echo "  make test         - Run unit tests (mvn test)"
	@echo "  make clean        - Clean build artifacts (mvn clean)"
	@echo "  make lint         - Run checkstyle lint checks (mvn checkstyle:check)"
	@echo "  make run-cli      - Run MultiToolAgent CLI"
	@echo "  make run-ui       - Run ADK Dev UI Web Server"
	@echo "  make docker-build - Build Docker image"

build:
	mvn compile

package:
	mvn package

test:
	mvn test

clean:
	mvn clean

lint:
	mvn checkstyle:check

run-cli:
	./cli.sh

run-ui:
	./devui.sh

docker-build:
	docker build -t adk-hello-world-java .
