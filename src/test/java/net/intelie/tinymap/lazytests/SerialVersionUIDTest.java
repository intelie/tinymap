package net.intelie.tinymap.lazytests;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SerialVersionUIDTest {
    @Test
    public void testSerialVersionUid() throws IOException {
        ClassPath cp = ClassPath.from(getClass().getClassLoader());
        ImmutableSet<ClassPath.ClassInfo> classes = cp.getAllClasses();

        for (ClassPath.ClassInfo info : classes) {
            if (!info.getPackageName().startsWith("net.intelie.tinymap")) continue;
            Class<?> clazz = info.load();

            boolean shouldHaveField = Serializable.class.isAssignableFrom(clazz) && !clazz.isEnum();

            boolean hasField = false;
            try {
                clazz.getDeclaredField("serialVersionUID");
                hasField = true;
            } catch (NoSuchFieldException ignored) {
            }

            assertThat(hasField)
                    .describedAs(clazz.getName())
                    .isEqualTo(shouldHaveField);
        }

    }
}
