COMPANY=smilo
AUTHOR=Elkan Roelen
NAME=api
PORT=8888
VERSION=latest
FULLDOCKERNAME=$(COMPANY)/$(NAME):$(VERSION)
DIR = $(shell pwd)

build:
	mvn clean install
	docker build --no-cache -t $(FULLDOCKERNAME) .

stop:
	docker stop -t 0 $(NAME)

clean:
	docker rm -f $(NAME)

cleanrestart: clean start

start:
	docker run -d --name=$(NAME) -h $(NAME) -p $(PORT):8080 $(FULLDOCKERNAME)

all: build clean start logs

logs:
	docker logs -f $(NAME)
