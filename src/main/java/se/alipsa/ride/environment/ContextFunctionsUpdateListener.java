package se.alipsa.ride.environment;

import se.alipsa.ride.utils.UniqueList;

import java.util.TreeSet;


public interface ContextFunctionsUpdateListener {

    void updateContextFunctions(TreeSet<String> contextFunctions, TreeSet<String> contaxtObjects);
}
