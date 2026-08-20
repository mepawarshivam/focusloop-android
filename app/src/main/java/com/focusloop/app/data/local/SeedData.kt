package com.focusloop.app.data.local

import com.focusloop.app.data.local.dao.LearningQuestionDao
import com.focusloop.app.data.local.entity.LearningQuestionEntity

object SeedData {
    suspend fun seedQuestions(dao: LearningQuestionDao) {
        if (dao.getCount() > 0) return // Already seeded

        val questions = dsaQuestions()
        dao.insertAll(questions)
    }

    private fun dsaQuestions() = listOf(
        LearningQuestionEntity(
            category = "DSA",
            question = "What is the average time complexity of searching a hash table?",
            option1 = "O(n)",
            option2 = "O(log n)",
            option3 = "O(1)",
            option4 = "O(n²)",
            correctAnswer = 2,
            explanation = "Hash tables provide O(1) average lookup time because a hash function maps keys directly to memory locations.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "What data structure uses LIFO (Last In, First Out) ordering?",
            option1 = "Queue",
            option2 = "Stack",
            option3 = "Heap",
            option4 = "Linked List",
            correctAnswer = 1,
            explanation = "A Stack uses LIFO ordering — the last element added is the first one removed. Think of a stack of plates.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "What is the time complexity of binary search?",
            option1 = "O(n)",
            option2 = "O(n²)",
            option3 = "O(1)",
            option4 = "O(log n)",
            correctAnswer = 3,
            explanation = "Binary search divides the search space in half each iteration, giving O(log n) time complexity.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "Which sorting algorithm has the best average-case time complexity?",
            option1 = "Bubble Sort — O(n²)",
            option2 = "Merge Sort — O(n log n)",
            option3 = "Insertion Sort — O(n²)",
            option4 = "Selection Sort — O(n²)",
            correctAnswer = 1,
            explanation = "Merge Sort guarantees O(n log n) in all cases by dividing the array and merging sorted halves.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "What is a balanced binary search tree optimized for?",
            option1 = "Memory efficiency",
            option2 = "O(1) access to min/max",
            option3 = "Guaranteed O(log n) operations",
            option4 = "Constant time insertion",
            correctAnswer = 2,
            explanation = "Balanced BSTs (like AVL, Red-Black trees) maintain height balance to guarantee O(log n) search, insert, and delete.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "In graph algorithms, what does BFS use to track nodes?",
            option1 = "Stack",
            option2 = "Queue",
            option3 = "Priority Queue",
            option4 = "Array",
            correctAnswer = 1,
            explanation = "BFS (Breadth-First Search) uses a Queue to explore nodes level by level.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "What is the space complexity of a recursive DFS on a graph with V vertices?",
            option1 = "O(1)",
            option2 = "O(V²)",
            option3 = "O(V)",
            option4 = "O(E)",
            correctAnswer = 2,
            explanation = "DFS uses the call stack, which can be at most O(V) deep — one frame per vertex in the worst case.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "Which data structure best supports finding the minimum element in O(1)?",
            option1 = "Hash Map",
            option2 = "Binary Search Tree",
            option3 = "Min-Heap",
            option4 = "Sorted Array",
            correctAnswer = 2,
            explanation = "A Min-Heap always keeps the smallest element at the root, so accessing it is O(1).",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "What is dynamic programming primarily used for?",
            option1 = "Sorting large datasets",
            option2 = "Solving problems with overlapping subproblems",
            option3 = "Graph traversal",
            option4 = "Memory allocation",
            correctAnswer = 1,
            explanation = "Dynamic programming solves problems by breaking them into overlapping subproblems and storing results to avoid recomputation.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "DSA",
            question = "What does the Big O notation O(1) indicate?",
            option1 = "The algorithm takes one step",
            option2 = "The algorithm never terminates",
            option3 = "Time is constant regardless of input size",
            option4 = "The algorithm uses one unit of memory",
            correctAnswer = 2,
            explanation = "O(1) means the operation takes the same amount of time regardless of how large the input is.",
            difficulty = "EASY"
        ),

        // System Design
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What does horizontal scaling mean?",
            option1 = "Adding more memory to a single server",
            option2 = "Adding more servers to distribute load",
            option3 = "Upgrading the CPU of existing servers",
            option4 = "Increasing network bandwidth",
            correctAnswer = 1,
            explanation = "Horizontal scaling (scaling out) means adding more machines. Vertical scaling (scaling up) means making existing machines more powerful.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What is a CDN primarily used for?",
            option1 = "Database replication",
            option2 = "User authentication",
            option3 = "Serving static assets closer to users",
            option4 = "Load balancing API requests",
            correctAnswer = 2,
            explanation = "A CDN (Content Delivery Network) caches static content at edge locations worldwide to reduce latency for users.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "In the CAP theorem, what does 'C' stand for?",
            option1 = "Concurrency",
            option2 = "Caching",
            option3 = "Consistency",
            option4 = "Computation",
            correctAnswer = 2,
            explanation = "CAP stands for Consistency, Availability, and Partition tolerance. Distributed systems can guarantee at most two of these.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What is the purpose of a message queue?",
            option1 = "Storing user session data",
            option2 = "Decoupling services and handling async processing",
            option3 = "Caching database queries",
            option4 = "Routing HTTP requests",
            correctAnswer = 1,
            explanation = "Message queues (like Kafka, RabbitMQ) decouple producers from consumers, enabling async processing and buffering load spikes.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What does 'eventual consistency' mean?",
            option1 = "Data is always immediately consistent",
            option2 = "The system never achieves consistency",
            option3 = "All nodes will eventually reach the same state",
            option4 = "Consistency is only guaranteed for writes",
            correctAnswer = 2,
            explanation = "Eventual consistency means that given no new updates, all replicas will eventually converge to the same value.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What is rate limiting used for in APIs?",
            option1 = "Improving database performance",
            option2 = "Preventing abuse and ensuring fair use",
            option3 = "Encrypting API responses",
            option4 = "Compressing request payloads",
            correctAnswer = 1,
            explanation = "Rate limiting restricts how many requests a client can make in a time window, preventing abuse and protecting service stability.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "In microservices, what is a service mesh?",
            option1 = "A database for service configuration",
            option2 = "Infrastructure layer handling service-to-service communication",
            option3 = "A UI framework for service dashboards",
            option4 = "A type of API gateway",
            correctAnswer = 1,
            explanation = "A service mesh (like Istio) handles communication between microservices, providing observability, security, and traffic management.",
            difficulty = "HARD"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What is the main benefit of database indexing?",
            option1 = "Reducing storage space",
            option2 = "Faster query lookups at the cost of write speed",
            option3 = "Automatic data backup",
            option4 = "Improved data security",
            correctAnswer = 1,
            explanation = "Indexes speed up read queries by creating sorted lookup structures, but they slow down writes because the index must be updated.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What does sharding in databases mean?",
            option1 = "Encrypting sensitive data",
            option2 = "Horizontal partitioning of data across multiple databases",
            option3 = "Compressing old records",
            option4 = "Synchronizing database replicas",
            correctAnswer = 1,
            explanation = "Sharding splits a large database into smaller pieces (shards) distributed across multiple servers to scale horizontally.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "SYSTEM_DESIGN",
            question = "What is a load balancer?",
            option1 = "A server that compresses responses",
            option2 = "A system that distributes requests across multiple servers",
            option3 = "A caching layer in front of a database",
            option4 = "A tool for monitoring server health",
            correctAnswer = 1,
            explanation = "A load balancer distributes incoming traffic across multiple backend servers to prevent any single server from being overwhelmed.",
            difficulty = "EASY"
        ),

        // JavaScript
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What is the difference between `==` and `===` in JavaScript?",
            option1 = "No difference",
            option2 = "`==` checks value only; `===` checks value and type",
            option3 = "`===` is for objects; `==` is for primitives",
            option4 = "`==` is deprecated",
            correctAnswer = 1,
            explanation = "== performs type coercion before comparing. === checks both value and type without coercion. Always prefer === for predictable comparisons.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What does `async/await` do in JavaScript?",
            option1 = "Runs code in parallel threads",
            option2 = "Syntax sugar for Promises, making async code readable",
            option3 = "Blocks the event loop",
            option4 = "Delays code execution by a fixed time",
            correctAnswer = 1,
            explanation = "async/await is syntactic sugar over Promises that makes asynchronous code look synchronous and easier to reason about.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What is a closure in JavaScript?",
            option1 = "A way to end a function",
            option2 = "A function that retains access to its outer scope",
            option3 = "A method to close browser windows",
            option4 = "An error handling pattern",
            correctAnswer = 1,
            explanation = "A closure is a function that remembers and accesses variables from its outer scope even after the outer function has returned.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What does the `Array.prototype.reduce()` method do?",
            option1 = "Filters array elements",
            option2 = "Transforms an array into a single value",
            option3 = "Sorts array elements",
            option4 = "Removes duplicate elements",
            correctAnswer = 1,
            explanation = "reduce() accumulates array values into a single result using a reducer function. Example: summing all numbers in an array.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What is the event loop in JavaScript?",
            option1 = "A for loop that handles DOM events",
            option2 = "A mechanism for handling async operations in a single-threaded environment",
            option3 = "A built-in event emitter",
            option4 = "A way to loop through event listeners",
            correctAnswer = 1,
            explanation = "The event loop monitors the call stack and callback queue, pushing callbacks to the stack when it's empty — enabling async JS in one thread.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What does `typeof null` return in JavaScript?",
            option1 = "\"null\"",
            option2 = "\"undefined\"",
            option3 = "\"object\"",
            option4 = "\"boolean\"",
            correctAnswer = 2,
            explanation = "typeof null === 'object' is a famous JavaScript bug from the language's early design. null is not actually an object.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What is the difference between `let` and `var` in JavaScript?",
            option1 = "No functional difference",
            option2 = "`let` has block scope; `var` has function scope",
            option3 = "`var` has block scope; `let` has function scope",
            option4 = "`let` is only for constants",
            correctAnswer = 1,
            explanation = "let is block-scoped and not hoisted in the same way as var. var is function-scoped and hoisted to the top of its function.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What is prototypal inheritance in JavaScript?",
            option1 = "A way to copy object properties",
            option2 = "Objects inheriting properties from a prototype object",
            option3 = "A type system for JavaScript",
            option4 = "Class-based inheritance",
            correctAnswer = 1,
            explanation = "JavaScript uses prototypal inheritance — objects can inherit properties and methods directly from other objects via the prototype chain.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What does the spread operator `...` do?",
            option1 = "Deletes array elements",
            option2 = "Expands an iterable into individual elements",
            option3 = "Merges two functions",
            option4 = "Creates an infinite loop",
            correctAnswer = 1,
            explanation = "The spread operator expands arrays/objects. For example, [...arr1, ...arr2] creates a new array combining both.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "JAVASCRIPT",
            question = "What is a Promise in JavaScript?",
            option1 = "A variable that promises not to change",
            option2 = "An object representing an eventually available value",
            option3 = "A way to guarantee synchronous execution",
            option4 = "A pattern for handling multiple events",
            correctAnswer = 1,
            explanation = "A Promise represents a value that may be available now, later, or never. It can be pending, fulfilled, or rejected.",
            difficulty = "EASY"
        ),

        // Programming
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is the SOLID principle 'S' (Single Responsibility)?",
            option1 = "A class should have many responsibilities",
            option2 = "A class should have only one reason to change",
            option3 = "Functions should accept single arguments only",
            option4 = "Variables should be declared once",
            correctAnswer = 1,
            explanation = "Single Responsibility means a class should have one reason to change — it should do one thing well. This makes code easier to test and maintain.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What does DRY stand for in software development?",
            option1 = "Data Recovery Yield",
            option2 = "Don't Repeat Yourself",
            option3 = "Distributed Runtime Yield",
            option4 = "Dynamic Resource Yielding",
            correctAnswer = 1,
            explanation = "DRY (Don't Repeat Yourself) means every piece of knowledge should have a single, unambiguous representation in your codebase.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is the difference between a compiled and interpreted language?",
            option1 = "No difference in execution speed",
            option2 = "Compiled languages translate code before runtime; interpreted during runtime",
            option3 = "Compiled languages run slower",
            option4 = "Interpreted languages cannot be distributed",
            correctAnswer = 1,
            explanation = "Compiled languages (C, Go) convert source to machine code before running. Interpreted languages (Python, JS) execute code line by line at runtime.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is a race condition?",
            option1 = "A performance optimization technique",
            option2 = "A bug where outcome depends on non-deterministic thread timing",
            option3 = "A sorting algorithm",
            option4 = "A database consistency model",
            correctAnswer = 1,
            explanation = "A race condition occurs when two threads access shared data simultaneously and the outcome depends on which one executes first.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is functional programming?",
            option1 = "Programming with only functions, no classes",
            option2 = "A paradigm treating computation as evaluating pure functions avoiding side effects",
            option3 = "Programs that work as intended",
            option4 = "Object-oriented programming with methods",
            correctAnswer = 1,
            explanation = "Functional programming uses pure functions, immutable data, and avoids side effects, making code more predictable and testable.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What does REST stand for?",
            option1 = "Remote Execution State Transfer",
            option2 = "Representational State Transfer",
            option3 = "Rapid Endpoint Service Technology",
            option4 = "Resource Entity State Transfer",
            correctAnswer = 1,
            explanation = "REST (Representational State Transfer) is an architectural style for distributed hypermedia systems using standard HTTP methods.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is the purpose of unit testing?",
            option1 = "Testing the entire system end-to-end",
            option2 = "Testing individual units of code in isolation",
            option3 = "Measuring application performance",
            option4 = "Testing UI components",
            correctAnswer = 1,
            explanation = "Unit tests verify that individual functions or classes behave correctly in isolation, catching bugs early and enabling safe refactoring.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is version control used for?",
            option1 = "Controlling software licensing versions",
            option2 = "Tracking and managing changes to code over time",
            option3 = "Managing application configurations",
            option4 = "Controlling API version deprecation",
            correctAnswer = 1,
            explanation = "Version control systems (like Git) track code changes, enable collaboration, and allow rolling back to previous versions.",
            difficulty = "EASY"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is dependency injection?",
            option1 = "Installing npm packages",
            option2 = "Providing dependencies to a class from outside rather than creating them internally",
            option3 = "A way to inject code at runtime",
            option4 = "Importing modules into a file",
            correctAnswer = 1,
            explanation = "Dependency injection means providing a class's dependencies externally rather than having it create them. This improves testability and flexibility.",
            difficulty = "MEDIUM"
        ),
        LearningQuestionEntity(
            category = "PROGRAMMING",
            question = "What is the Observer pattern?",
            option1 = "A pattern for logging application behavior",
            option2 = "A pattern where objects subscribe to events from another object",
            option3 = "A performance monitoring pattern",
            option4 = "A data access pattern",
            correctAnswer = 1,
            explanation = "The Observer pattern defines a one-to-many dependency so when one object changes state, all its dependents are notified automatically.",
            difficulty = "MEDIUM"
        )
    )

}
