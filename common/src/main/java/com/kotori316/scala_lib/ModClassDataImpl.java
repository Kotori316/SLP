package com.kotori316.scala_lib;

import java.util.Set;

record ModClassDataImpl<DIST>(
    String className,
    String modID,
    Set<DIST> availableDistSet
) implements ModClassData<DIST> {
}
