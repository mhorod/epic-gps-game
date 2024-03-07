package server;

import java.util.List;

record MonstersMessage(List<Monster> monsters) implements Message { }
