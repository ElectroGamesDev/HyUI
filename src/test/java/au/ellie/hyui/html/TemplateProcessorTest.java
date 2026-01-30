package au.ellie.hyui.html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateProcessorTest {

    private TemplateProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TemplateProcessor();
    }

    /* --------------------------------------------------
     * Variable substitution
     * -------------------------------------------------- */

    @Test
    void replacesSimpleVariable() {
        processor.setVariable("name", "Ellie");

        assertEquals(
                "Hello Ellie!",
                processor.process("Hello {{$name}}!")
        );
    }

    @Test
    void usesDefaultValueWhenVariableIsMissing() {
        assertEquals(
                "Score: 0",
                processor.process("Score: {{$score|0}}")
        );
    }

    /* --------------------------------------------------
     * Filters
     * -------------------------------------------------- */

    @Nested
    class StringFilters {

        @Test
        void upper_convertsToUppercase() {
            processor.setVariable("value", "hello");

            assertEquals(
                    "HELLO",
                    processor.process("{{$value|upper}}")
            );
        }

        @Test
        void lower_convertsToLowercase() {
            processor.setVariable("value", "HeLLo");

            assertEquals(
                    "hello",
                    processor.process("{{$value|lower}}")
            );
        }

        @Test
        void trim_removesLeadingAndTrailingWhitespace() {
            processor.setVariable("value", "  hello   ");

            assertEquals(
                    "hello",
                    processor.process("{{$value|trim}}")
            );
        }

        @Test
        void capitalize_capitalizesFirstLetterOnly() {
            processor.setVariable("value", "hello world");

            assertEquals(
                    "Hello world",
                    processor.process("{{$value|capitalize}}")
            );
        }
    }

    @Nested
    class NumberFilters {

        @Test
        void number_formatsNumber() {
            processor.setVariable("value", 1234);

            assertEquals(
                    "1 234",
                    processor.process("{{$value|number}}")
            );
        }

        @Test
        void percent_formatsPercent() {
            processor.setVariable("value", 0.125);

            assertEquals(
                    "13%",
                    processor.process("{{$value|percent}}")
            );
        }
    }

    /* --------------------------------------------------
     * If blocks
     * -------------------------------------------------- */

    @Nested
    class IfBlocks {

        @Test
        void rendersTrueBranch() {
            processor.setVariable("loggedIn", true);

            String template = """
                    {{#if loggedIn}}
                    Welcome back!
                    {{else}}
                    Please log in
                    {{/if}}
                    """;

            assertEquals(
                    "Welcome back!",
                    processor.process(template).trim()
            );
        }

        @Test
        void rendersFalseBranch() {
            processor.setVariable("loggedIn", false);

            String template = """
                    {{#if loggedIn}}
                    Welcome!
                    {{else}}
                    Please log in
                    {{/if}}
                    """;

            assertEquals(
                    "Please log in",
                    processor.process(template).trim()
            );
        }

        @Test
        void withoutElse_rendersNothingWhenFalse() {
            processor.setVariable("enabled", false);

            String template = """
                    Before
                    {{#if enabled}}
                    Enabled
                    {{/if}}
                    After
                    """;

            String result = processor.process(template)
                    .replaceAll("\\s+", " ")
                    .trim();

            assertEquals("Before After", result);
        }
    }

    /* --------------------------------------------------
     * Each blocks
     * -------------------------------------------------- */

    @Nested
    class EachBlocks {

        @Test
        void iteratesOverList() {
            processor.setVariable("items", List.of("A", "B", "C"));

            String template = """
                    {{#each items}}
                    <span>{{$item}}</span>
                    {{/each}}
                    """;

            String result = processor.process(template)
                    .replaceAll("\\s+", "");

            assertEquals(
                    "<span>A</span><span>B</span><span>C</span>",
                    result
            );
        }

        @Test
        void exposesMapEntriesAsVariables() {
            processor.setVariable(
                    "users",
                    List.of(
                            Map.of("name", "Alice"),
                            Map.of("name", "Bob")
                    )
            );

            String template = """
                    {{#each users}}
                    {{$name}}
                    {{/each}}
                    """;

            assertEquals(
                    "AliceBob",
                    processor.process(template).replaceAll("\\s+", "")
            );
        }
    }

    /* --------------------------------------------------
     * Components
     * -------------------------------------------------- */

    @Nested
    class Components {

        @Test
        void expandsComponentWithParameters() {
            processor.registerComponent(
                    "button",
                    "<button id=\"{{$id}}\">{{$text}}</button>"
            );

            assertEquals(
                    "<button id=\"myBtn\">Click Me</button>",
                    processor.process("{{@button:text=Click Me,id=myBtn}}")
            );
        }

        @Test
        void componentCanAccessVariablesFromScope() {
            processor
                    .setVariable("label", "Submit")
                    .registerComponent("button", "<button>{{$label}}</button>");

            assertEquals(
                    "<button>Submit</button>",
                    processor.process("{{@button}}")
            );
        }
    }

    /* --------------------------------------------------
     * Supplier laziness
     * -------------------------------------------------- */

    @Nested
    class SupplierEvaluation {

        @Test
        void supplierIsNotEvaluatedWhenIfConditionIsFalse() {
            AtomicInteger evaluations = new AtomicInteger();

            processor
                    .setVariable("enabled", false)
                    .setVariable("secret", () -> {
                        evaluations.incrementAndGet();
                        return "SHOULD NOT HAPPEN";
                    });

            String template = """
                    {{#if enabled}}
                    {{$secret}}
                    {{/if}}
                    """;

            assertEquals("", processor.process(template).trim());
            assertEquals(0, evaluations.get(), "Supplier must not be evaluated");
        }

        @Test
        void supplierIsEvaluatedWhenIfConditionIsTrue() {
            AtomicInteger evaluations = new AtomicInteger();

            processor
                    .setVariable("enabled", true)
                    .setVariable("value", () -> {
                        evaluations.incrementAndGet();
                        return "OK";
                    });

            String template = """
                    {{#if enabled}}
                    {{$value}}
                    {{/if}}
                    """;

            assertEquals("OK", processor.process(template).trim());
            assertEquals(1, evaluations.get());
        }
    }

    /* --------------------------------------------------
     * Combined scenario
     * -------------------------------------------------- */

    @Test
    void complexTemplateRendersCorrectly() {
        processor
                .setVariable("player", "Ellie")
                .setVariable("online", true)
                .setVariable("scores", List.of(10, 20))
                .registerComponent("score", "<li>{{$item}}</li>");

        String template = """
                <h1>Hello {{$player}}</h1>
                
                {{#if online}}
                <ul>
                    {{#each scores}}
                        {{@score}}
                    {{/each}}
                </ul>
                {{else}}
                Offline
                {{/if}}
                """;

        String result = processor.process(template)
                .replaceAll("\\s+", "");

        assertEquals(
                "<h1>HelloEllie</h1><ul><li>10</li><li>20</li></ul>",
                result
        );
    }
}
