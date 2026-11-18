package com.bracits.pms.shared;

import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.action.Result;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The three-step contract of BaseAction (guide §3.4, "What happens" table). */
class BaseActionTest {

    /** Records which steps ran. */
    static class RecordingAction extends BaseAction<String, String> {
        final List<String> steps = new ArrayList<>();

        @Override
        protected String preCondition(String input) {
            steps.add("pre");
            return input.isBlank() ? "Input is required" : null;
        }

        @Override
        protected String execute(String input) {
            steps.add("execute");
            if (input.equals("boom")) {
                throw new IllegalStateException("DB error");
            }
            return input.toUpperCase();
        }

        @Override
        protected void postCondition(String input, String output) {
            steps.add("post");
        }

        @Override
        protected String successMessage(String output) {
            return "Saved " + output;
        }
    }

    @Test
    void invalidInputStopsBeforeExecute() {
        var action = new RecordingAction();
        Result<String> result = action.run(" ");
        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("Input is required");
        assertThat(action.steps).containsExactly("pre");           // execute and post never ran
    }

    @Test
    void validInputRunsAllThreeStepsInOrder() {
        var action = new RecordingAction();
        Result<String> result = action.run("ok");
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("OK");
        assertThat(result.message()).isEqualTo("Saved OK");
        assertThat(action.steps).containsExactly("pre", "execute", "post");
    }

    @Test
    void failureInExecuteSkipsPostCondition() {
        var action = new RecordingAction();
        assertThatThrownBy(() -> action.run("boom")).isInstanceOf(IllegalStateException.class);
        assertThat(action.steps).containsExactly("pre", "execute");  // no mail after a failed save
    }
}
