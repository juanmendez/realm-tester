package info.juanmendez.mockrealm.dependencies;

import org.mockito.ArgumentMatcher;

/**
 * Created by @juanmendezinfo on 2/15/2017.
 */
public class RealmMatchers {

    /**
     * argument matcher checks if targetClass is the super class of the object passed.
     * @param <T>
     */
    public static class ClassMatcher<T> extends ArgumentMatcher<Class<T>> {

        private final Class<T> targetClass;

        public ClassMatcher(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        public boolean matches(Object obj) {

            if (obj != null && obj instanceof Class) {
                return targetClass.isAssignableFrom((Class<T>) obj);
            }
            return false;
        }
    }

    /**
     * checks if instance matches class
     * @param <T>
     */
    class InstanceMatcher<T> extends ArgumentMatcher<T>{

        private final Class<T> targetClass;

        public InstanceMatcher(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public boolean matches(Object o) {
            return targetClass.isInstance( o );
        }
    }
}
