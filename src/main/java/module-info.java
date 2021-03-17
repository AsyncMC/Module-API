module com.github.asyncmc.module.api {
    exports com.github.asyncmc.module.api;

    requires transitive kotlin.stdlib;
    requires transitive kotlinx.coroutines.core.jvm;
    requires transitive kotlin.inline.logger.jvm;
}
