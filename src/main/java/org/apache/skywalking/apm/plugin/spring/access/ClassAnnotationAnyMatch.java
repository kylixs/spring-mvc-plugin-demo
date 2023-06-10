package org.apache.skywalking.apm.plugin.spring.access;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.match.IndirectMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class ClassAnnotationAnyMatch implements IndirectMatch {
    private String[] annotations;

    private ClassAnnotationAnyMatch(String[] annotations) {
        if (annotations == null || annotations.length == 0) {
            throw new IllegalArgumentException("annotations is null");
        }
        this.annotations = annotations;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        for (String annotation : annotations) {
            if (junction == null) {
                junction = buildEachAnnotation(annotation);
            } else {
                junction = junction.or(buildEachAnnotation(annotation));
            }
        }
        junction = not(isInterface()).and(junction);
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        List<String> annotationList = new ArrayList<String>(Arrays.asList(annotations));
        AnnotationList declaredAnnotations = typeDescription.getDeclaredAnnotations();
        for (AnnotationDescription annotation : declaredAnnotations) {
            annotationList.remove(annotation.getAnnotationType().getActualName());
        }
        return annotationList.isEmpty();
    }

    private ElementMatcher.Junction buildEachAnnotation(String annotationName) {
        return isAnnotatedWith(named(annotationName));
    }

    public static ClassAnnotationAnyMatch byClassAnnotationAnyMatch(String... annotations) {
        return new ClassAnnotationAnyMatch(annotations);
    }
}