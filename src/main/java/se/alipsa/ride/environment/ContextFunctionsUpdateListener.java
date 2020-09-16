package se.alipsa.ride.environment;

import se.alipsa.ride.utils.UniqueList;


public interface ContextFunctionsUpdateListener {

    void updateContextFunctions(UniqueList<String> contextFunctions, UniqueList<String> contaxtObjects);
}
