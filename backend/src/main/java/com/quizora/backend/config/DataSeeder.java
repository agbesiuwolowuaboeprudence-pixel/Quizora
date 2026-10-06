package com.quizora.backend.config;

import com.quizora.backend.domain.*;
import com.quizora.backend.repository.*;
import com.quizora.backend.service.SubscriptionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds demo data on first start so the API can be explored immediately:
 *  - platform admin, demo student, demo institution + licence code
 *  - JHS subjects and a sample bank of BECE-style objective questions
 *  - motivational quotes for the daily dashboard quote
 */
@Component
@ConditionalOnProperty(name = "quizora.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final MotivationalQuoteRepository quoteRepository;
    private final InstitutionRepository institutionRepository;
    private final LicenseCodeRepository licenseCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

    public DataSeeder(UserRepository userRepository,
                      CourseRepository courseRepository,
                      QuestionRepository questionRepository,
                      MotivationalQuoteRepository quoteRepository,
                      InstitutionRepository institutionRepository,
                      LicenseCodeRepository licenseCodeRepository,
                      PasswordEncoder passwordEncoder,
                      SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.quoteRepository = quoteRepository;
        this.institutionRepository = institutionRepository;
        this.licenseCodeRepository = licenseCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedQuotes();
        seedCoursesAndQuestions();
        seedInstitution();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmailIgnoreCase("admin@quizora.app")) {
            userRepository.save(new User("Platform Admin", "admin@quizora.app",
                    passwordEncoder.encode("Admin@123"), Role.ADMIN));
        }
        if (!userRepository.existsByEmailIgnoreCase("student@demo.com")) {
            User student = new User("Kofi Mensah", "student@demo.com",
                    passwordEncoder.encode("Student@123"), Role.STUDENT);
            userRepository.save(student);
            subscriptionService.activate(student, PlanType.YEARLY, 365, "DEMO-PRELOADED");
        }
    }

    private void seedQuotes() {
        if (quoteRepository.count() > 0) return;
        quoteRepository.saveAll(List.of(
                new MotivationalQuote("Education is the key that unlocks the golden door to freedom.", "George Washington Carver"),
                new MotivationalQuote("The expert in anything was once a beginner.", "Helen Hayes"),
                new MotivationalQuote("Little by little, the bird builds its nest.", "Ghanaian proverb"),
                new MotivationalQuote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
                new MotivationalQuote("It always seems impossible until it is done.", "Nelson Mandela"),
                new MotivationalQuote("The more you read, the more things you will know.", "Dr. Seuss"),
                new MotivationalQuote("However long the night, the dawn will break.", "African proverb"),
                new MotivationalQuote("An investment in knowledge pays the best interest.", "Benjamin Franklin"),
                new MotivationalQuote("Proper preparation prevents poor performance.", "Proverb"),
                new MotivationalQuote("Knowledge is power.", "Francis Bacon"),
                new MotivationalQuote("Procrastination is the thief of time.", "Edward Young"),
                new MotivationalQuote("Practice makes progress.", "Proverb")));
    }

    private void seedCoursesAndQuestions() {
        if (courseRepository.count() > 0) return;

        Course maths = course("Mathematics", "MATHS", "BECE mathematics - number, algebra, geometry, mensuration");
        Course science = course("Integrated Science", "SCIENCE", "BECE integrated science - physical and biological science");
        Course english = course("English Language", "ENGLISH", "BECE English language - grammar, vocabulary, comprehension");
        Course social = course("Social Studies", "SOCIAL", "BECE social studies - governance, environment, national development");
        Course ict = course("ICT", "ICT", "BECE information and communication technology");
        Course arts = course("Creative Arts", "CREATIVE", "BECE creative arts - visual arts, music, drama");

        // ---------------- Mathematics 2023
        q(maths, 2023, "Simplify: 2/3 + 3/4", "17/12", "5/7", "21/12", "11/12", AnswerOption.A,
                "L.C.M. of 3 and 4 is 12: 8/12 + 9/12 = 17/12.");
        q(maths, 2023, "Evaluate 15% of GH\u00A2200", "GH\u00A215", "GH\u00A225", "GH\u00A230", "GH\u00A235", AnswerOption.C,
                "15/100 \u00D7 200 = 30.");
        q(maths, 2023, "Factorise x\u00B2 - 9", "(x - 3)(x + 3)", "(x - 9)(x + 1)", "(x - 3)\u00B2", "(x + 3)\u00B2", AnswerOption.A,
                "Difference of two squares: a\u00B2 - b\u00B2 = (a - b)(a + b).");
        q(maths, 2023, "Find the median of 3, 5, 7, 9, 11", "5", "7", "9", "3", AnswerOption.B,
                "The middle value of the ordered list is 7.");
        q(maths, 2023, "Solve for x: 2x + 4 = 10", "x = 2", "x = 3", "x = 4", "x = 7", AnswerOption.B,
                "2x = 6, so x = 3.");
        q(maths, 2023, "Find the area of a triangle with base 10 cm and height 6 cm", "16 cm\u00B2", "60 cm\u00B2", "30 cm\u00B2", "15 cm\u00B2", AnswerOption.C,
                "Area = \u00BD \u00D7 base \u00D7 height = \u00BD \u00D7 10 \u00D7 6 = 30 cm\u00B2.");
        q(maths, 2023, "Find the simple interest on GH\u00A2500 at 10% per annum for 2 years", "GH\u00A250", "GH\u00A2100", "GH\u00A225", "GH\u00A2200", AnswerOption.B,
                "I = PRT/100 = 500 \u00D7 10 \u00D7 2 / 100 = GH\u00A2100.");
        q(maths, 2023, "Simplify \u221A144", "10", "11", "12", "14", AnswerOption.C,
                "12 \u00D7 12 = 144.");

        // ---------------- Mathematics 2024
        q(maths, 2024, "Convert 0.75 to a fraction in its lowest terms", "1/2", "3/4", "2/3", "4/5", AnswerOption.B,
                "0.75 = 75/100 = 3/4.");
        q(maths, 2024, "Find the perimeter of a square of side 8 cm", "16 cm", "64 cm", "32 cm", "24 cm", AnswerOption.C,
                "Perimeter = 4 \u00D7 side = 4 \u00D7 8 = 32 cm.");
        q(maths, 2024, "If 3 books cost GH\u00A212, how much do 5 books cost?", "GH\u00A215", "GH\u00A218", "GH\u00A225", "GH\u00A220", AnswerOption.D,
                "Cost of 1 book = GH\u00A24, so 5 books = GH\u00A220.");
        q(maths, 2024, "An angle inscribed in a semicircle measures", "90\u00B0", "45\u00B0", "60\u00B0", "180\u00B0", AnswerOption.A,
                "Thales' theorem: an angle in a semicircle is a right angle.");

        // ---------------- Integrated Science 2023
        q(science, 2023, "Which of the following is a mammal?", "Snake", "Goat", "Chicken", "Toad", AnswerOption.B,
                "Mammals have hair/fur and feed their young on milk.");
        q(science, 2023, "The process by which green plants make their food is called", "Respiration", "Transpiration", "Photosynthesis", "Germination", AnswerOption.C,
                "Photosynthesis uses sunlight, water and carbon dioxide.");
        q(science, 2023, "The SI unit of force is the", "Joule", "Newton", "Watt", "Pascal", AnswerOption.B,
                "Force is measured in newtons (N).");
        q(science, 2023, "Which gas do plants absorb from the air during photosynthesis?", "Oxygen", "Nitrogen", "Hydrogen", "Carbon dioxide", AnswerOption.D,
                "Carbon dioxide is taken in through the stomata.");
        q(science, 2023, "Which part of the cell controls its activities?", "Cell wall", "Cytoplasm", "Vacuole", "Nucleus", AnswerOption.D,
                "The nucleus contains the genetic material and controls the cell.");
        q(science, 2023, "Which of these is a renewable source of energy?", "Coal", "Solar energy", "Petroleum", "Natural gas", AnswerOption.B,
                "Solar energy is continuously replenished by the sun.");
        q(science, 2023, "The normal temperature of the human body is about", "32\u00B0C", "35\u00B0C", "37\u00B0C", "40\u00B0C", AnswerOption.C,
                "Normal body temperature is about 37\u00B0C (98.6\u00B0F).");
        q(science, 2023, "At sea level, water boils at", "100\u00B0C", "50\u00B0C", "0\u00B0C", "212\u00B0C", AnswerOption.A,
                "Water boils at 100\u00B0C at standard atmospheric pressure.");

        // ---------------- Integrated Science 2024
        q(science, 2024, "The pH of pure water is", "0", "7", "10", "14", AnswerOption.B,
                "Pure water is neutral with pH 7.");
        q(science, 2024, "Which organ pumps blood around the body?", "Lungs", "Liver", "Heart", "Kidney", AnswerOption.C,
                "The heart is the muscular pump of the circulatory system.");
        q(science, 2024, "The substance in red blood cells that carries oxygen is called", "Haemoglobin", "Plasma", "Fibrinogen", "Antibody", AnswerOption.A,
                "Haemoglobin binds oxygen in the lungs.");
        q(science, 2024, "The layer of the atmosphere in which we live is the", "Stratosphere", "Mesosphere", "Thermosphere", "Troposphere", AnswerOption.D,
                "The troposphere is the lowest layer, up to about 12 km.");

        // ---------------- English Language 2023
        q(english, 2023, "Choose the correct plural of 'child'", "childs", "children", "childes", "childrens", AnswerOption.B,
                "Child has the irregular plural children.");
        q(english, 2023, "Choose the word opposite in meaning to 'ancient'", "old", "historic", "modern", "past", AnswerOption.C,
                "Ancient means very old, so its antonym is modern.");
        q(english, 2023, "Select the correct option: The dogs ___ barking loudly.", "was", "has been", "were", "is", AnswerOption.C,
                "'Dogs' is plural, so the plural past continuous verb 'were' is correct.");
        q(english, 2023, "Choose the correctly spelt word", "accomodate", "accommodate", "acommodate", "accomadate", AnswerOption.B,
                "Accommodate has double c and double m.");
        q(english, 2023, "The idiom 'to keep one's chin up' means to", "eat well", "sleep early", "bow down", "remain hopeful", AnswerOption.D,
                "It means to stay cheerful and optimistic in difficulty.");
        q(english, 2023, "A person who writes poems is called a", "novelist", "poet", "journalist", "dramatist", AnswerOption.B,
                "A poet writes poems.");

        // ---------------- Social Studies 2023
        q(social, 2023, "Ghana gained independence in", "1955", "1957", "1960", "1966", AnswerOption.B,
                "Ghana became independent on 6 March 1957.");
        q(social, 2023, "The first president of Ghana and leader of the independence movement was", "Kwame Nkrumah", "Edward Akufo-Addo", "Jerry Rawlings", "John Atta Mills", AnswerOption.A,
                "Dr. Kwame Nkrumah led Ghana to independence in 1957.");
        q(social, 2023, "The branch of government that makes laws is the", "Judiciary", "Executive", "Legislature", "Civil service", AnswerOption.C,
                "Parliament (the legislature) makes laws.");
        q(social, 2023, "Which body conducts elections in Ghana?", "The Police Service", "The Judiciary", "District Assemblies", "The Electoral Commission", AnswerOption.D,
                "The Electoral Commission organises and supervises elections.");
        q(social, 2023, "The Atlantic Ocean lies to the ___ of Ghana", "north", "south", "east", "west", AnswerOption.B,
                "Ghana's southern border faces the Atlantic Ocean.");

        // ---------------- ICT 2023
        q(ict, 2023, "CPU stands for", "Central Processing Unit", "Computer Personal Unit", "Central Power Utility", "Computer Program Utility", AnswerOption.A,
                "The CPU is the main processor of the computer.");
        q(ict, 2023, "Which of these is an input device?", "Printer", "Monitor", "Keyboard", "Speaker", AnswerOption.C,
                "A keyboard enters data into the computer.");
        q(ict, 2023, "1 kilobyte (KB) is equal to", "100 bytes", "1024 bytes", "1024 megabytes", "8 bytes", AnswerOption.B,
                "1 KB = 1024 bytes.");
        q(ict, 2023, "Which of the following is system software?", "Microsoft Word", "Adobe Photoshop", "Operating System", "Google Chrome", AnswerOption.C,
                "An operating system manages the computer's hardware and software.");
        q(ict, 2023, "HTML is used for", "designing databases", "compiling Java programs", "managing networks", "creating web pages", AnswerOption.D,
                "HTML is the markup language of web pages.");

        // ---------------- Creative Arts 2023
        q(arts, 2023, "Kente cloth is traditionally woven by the ___ people", "Fante", "Ewe", "Nzema", "Dagomba", AnswerOption.B,
                "Kente is associated with the Ashanti and Ewe peoples.");
        q(arts, 2023, "The art of shaping clay to make pots and statues is called", "Painting", "Weaving", "Sculpture", "Pottery/Modelling", AnswerOption.D,
                "Working with clay is modelling/pottery, a sculptural art.");
        q(arts, 2023, "The repetition of sound patterns in music is called", "Rhythm", "Melody", "Harmony", "Tempo", AnswerOption.A,
                "Rhythm is the pattern of beats in time.");
        q(arts, 2023, "Kpanlogo is a traditional dance of the ___ people", "Ga", "Ashanti", "Ewe", "Fante", AnswerOption.A,
                "Kpanlogo originated with the Ga people of Accra.");
    }

    private void seedInstitution() {
        if (institutionRepository.count() > 0) return;

        Institution institution = institutionRepository.save(new Institution(
                "Demo JHS Accra", "Ama Owusu", "school@demo.com", "0240000000"));

        User admin = new User("Ama Owusu", "school@demo.com",
                passwordEncoder.encode("School@123"), Role.INSTITUTION_ADMIN);
        admin.setInstitution(institution);
        userRepository.save(admin);

        LicenseCode license = licenseCodeRepository.save(new LicenseCode(
                "DEMO-CODE-1234", institution, 20,
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 12, 31)));

        // one student already onboarded with the code (seat 1 of 20 used)
        User student = new User("Kwesi Asante", "kofi@demo.com",
                passwordEncoder.encode("Student@123"), Role.STUDENT);
        student.setInstitution(institution);
        student.setLicenseCode(license);
        userRepository.save(student);
        license.setUsedSeats(1);
        licenseCodeRepository.save(license);
        subscriptionService.createInstitutional(student, license);
    }

    private Course course(String name, String code, String description) {
        return courseRepository.save(new Course(name, code, description, Level.JHS));
    }

    private void q(Course course, int year, String text, String a, String b, String c, String d,
                   AnswerOption correct, String explanation) {
        questionRepository.save(new Question(course, year, QuestionSection.OBJECTIVE,
                text, a, b, c, d, correct, explanation, 1));
    }
}
