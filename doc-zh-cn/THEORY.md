# 理论基础

本文讨论Shelly库的理论基础，包含Shelly库的思想以及相关技术术语的定义。

## 思想

在面向业务逻辑的编程中，某个特定的业务对象的改变可能会引起各个组件的改变，业务逻辑的复杂性会增加组件之间的耦合。为了降低耦合，我们通常使用listener（observer）或者event bus，这些技术易于使用并且非常有效。但是存在以下缺点：

1. listener或者event数量随着业务逻辑的复杂性的增加而增加。这样使工程难以维护。

2. 使用了某个listener，就需要相应的组件实现listener的接口，这样会使代码变得复杂。而且listener的滥用导致内存泄漏。

3. event bus的使用使代码变得难以调试，因为很难预测并且控制event发送之后会发生什么。你必须在IDE中寻找使用event的地方来确定哪些组件接收了这个event。

为了解决这些问题，我做了Shelly库。

Shelly库提供了一种全新的编程模式，将业务对象的变化对各个模块的影响通过方法链表示出来。在方法链中，每个方法的参数是代表某个组件变化的action。因此这个方法链代表了所有对应组件的变化。这样你就可以在一个文件中看到整个“世界”的变化，不需要搜索整个工程查找对应的类。

具体地说，一个方法链对应一条业务逻辑和一个业务对象。它表示，如果这个业务对象被改变，根据这条业务逻辑，整个app会有什么变化。

当方法链被创建的时候，应该指定这个业务对象的类，然后将每个方法附加到方法链的末尾。当Domino被调用的时候（下文将会提到），方法链中的action获取一些对象作为参数，执行相应的操作。

我们将更多的注意力放在每个action的输入上。方法链的第一个action将调用Domino时传入的业务对象作为参数，然后在它被执行后，它将业务对象传给下一个action，这个action也执行某个操作并且将业务对象传给下一个action，所以业务对象在action之间被传递，直到它们被传递到做数据变化的action为止。这个action将业务对象作为参数并且返回一个或一个以上的新对象。在变化后，新的对象被传递给随后的action，旧的对象被丢弃。

现在我们看看action。action可以被看作一个方法，这个方法将传递给它的对象作为输入，执行action内部的语句。Shelly库也提供一个类似EventBus的特色，即提供一些特殊action，这些action将一些注册的组件和传递给action的对象一起作为输入，执行action内部的语句。

Shelly库提供了许多方法来构造一个方法链，包括执行不同操作的各类方法、执行数据转换的方法和执行线程调度的方法。像上文提到的那样，它也提供一个类似EventBus的特性来在已注册的组件上执行action。

方法链提供来一个全局视角，通过它你可以看清一个特定业务对象改变后，整个app产生了什么变化。方法链中的方法均返回一个“Domino”对象。它表示一个将被一个个执行的action序列，和domino（多米诺）游戏类似。

在创建Domino后，你可以“调用”它来执行指定的操作。当一个业务对象被改变后，你调用对应的Domino，将业务对象传递给它，然后它就一个个执行action序列中的action。

## 与RxJava比较

在开发Shelly库的过程中，我发现了RxJava。于是我研究了并且借鉴了它的思想和实现方法。因此Shelly库和RxJava的风格在某种程度上相似，但它们的思想、实现和用法很不同，将在下面几个方面进行描述：

1. 开发的初衷不同。Shelly库开发的初衷是解决异步操作的回调中使用EventBus或者listener后导致的代码难以维护的问题。RxJava并不能解决这个问题。

2. 实现原理不同。Shelly库的`Domino`源码和RxJava库的`Observable`源码在某些程度上很像。但是因为Shelly和RxJava库用法不同，所以源码还是存在很多不同的，尤其是，`Domino`有两个泛型参数，并且有一个Tile函数作为它的成员，这个函数在调用后返回一个`Player`。毕竟，正是这些不同才使得Shelly库能工作。

3. 被执行的操作不同。Shelly库的`Domino`有很多方法和RxJava的`Observable`相似，这些方法用来数据流控制和线程调度。但是，`Domino`有很多方法用来一次性执行不同action，这个特点是RxJava没有的。具体地说，`Domino`的`perform`方法和`Observable`的`subscribe`方法很像。Shelly库中，你可以在方法链后添加多个`perform`方法来使得`Domino`在被调用后执行多个操作，但是RxJava中，你只能在方法链后添加一个`subscribe`方法，因此只能执行一个action。

4. Shelly库有类似EventBus的特点。Shelly库有一个类似EventBus的特点，允许你注册组件然后让`Domino`对已经注册的组件执行action，这样地话你在一个方法链中可以把所有的组件变化都写进去。RxJava不支持这个特性，使用RxJava改变组件的时候，你必须使用EventBus或者listener。

5. 触发时机不同。`Domino`执行action的时机和`Observable`订阅`Subscriber`的时机不同。Shelly库中，`Domino`在被提交时不会执行任何action。只有在被调用的时候才会执行action。因此你必须先创建并提交`Domino`，一旦`Domino`被提交，它就可以在任何时候被调用，也可以被调用多次。但是RxJava中，一旦`Observable.subscribe()`方法被调用，一切都会立即生效。另外，每次你要执行某个操作，你必须创建`Observable`并且让它订阅`Subscriber`。

## 定义

本节给出了关于Shelly库的技术术语的定义。（以下部分中英对照比较好，中文表达得不是很清晰。）

### 关于action

An "action" refers to a sequence of Java statements, the effect of which includes but is not limited
to, performing certain operations on certain components, producing certain outputs, performing
data transformation, and performing thread scheduling. An action is represented by a Java
class or a Java interface, in which there exists a method which is named "call" and contains the
sequence of Java statements of the action. Such a method may be invoked to perform the corresponding
action.

一个“action”（“操作”）指的是一个Java语句的序列，这个序列产生的效果包括但不限于，在某些组件上执行某些操作，产生某些输出，执行数据变换，以及执行线程调度。一个action由一个Java类或者Java接口表示，这个类或接口内存在一个名为“call”的、包含该action的Java语句序列的方法。这样的方法可以被调用以执行对应的action。

"Performing an action" refers to executing the sequence of Java statements of the action.

“执行一个action”指的是执行action的Java语句序列。（水平有限，“perform”和“execute”是由区别的，但是我把它们都翻译成“执行”，看英文版的效果会更好。）

### 关于Domino

A "Domino" is a Java object which, under certain circumstances, performs a sequence of actions.
For the sake of simplicity, a "Domino" may also refer to the Java class of a particular Domino
object.

一个“Domino”指的是一个在某种情况下会执行一个action序列的Java对象。为了简洁，一个“Domino”也可以指某个Domino对象的Java类。

"Creating a Domino" refers to the operation of building a Java instance of a particular Domino
class. A Domino is usually created by a Java method chain which starts with
`Shelly.createDomino(Object)` or `Shelly.createAnonymousDomino()` and is followed by various methods provided
by the Shelly library.

“创建一个Domino”指的是制造一个特定Domino类的一个Java实例。一个Domino总是通过一条以`Shelly.createDomino(Object)`或`Shelly.createAnonymousDomino()`开头，后面跟了由Shelly库提供的不同方法组成的Java方法链创建。

"Committing a Domino" refers to the operation of causing the Shelly library to hold a Java reference
of the particular Domino object for later use.

“提交一个Domino”指的是为了以后使用而使Shelly库持有一个特定的Domino对象的操作。

"Playing a Domino" or "Invoking a Domino" refers to the operation of causing the particular Domino to
perform a sequence of actions. To play a particular Domino, it is always necessary to pass a group
of objects to the Domino as its input. The group must contain one or more objects.

“玩一个Domino”或“调用一个Domino”指的是使一个特定的Domino执行一个action序列的操作。要玩一个Domino，总是需要传给这个Domino一组对象作为输入，这个组必须包含一个或一个以上的对象。

### 关于输入、输出和数据流

#### action的输入

The "input of an action" is a group ("input group") of objects which is passed to the `call` method
of the action as arguments.
The following illustrates the relationship between the input and the performance of an action:

一个“action的输入”是一组被传入action的`call`方法作为参数的对象。这个组叫做“输入组”。下文阐述了action输入和action执行之间的关系。


Suppose
the number of arguments the `call` method takes, excluding the arguments representing the components, is `a`,
the number of the objects contained in the input group is `b`,
and the number of occasions when the action is performed is `c`, i.e. the action is performed for `c` times.
Then

假设`call`方法中非组件的参数数目是`a`，输入组中包含的对象数目是`b`，action执行的次数`c`。那么

1. If b = 0 and a = 0, then c = 1.

2. If b = 0 and a > 0, then c = 0.

3. If b > 0 and a = 0, then c = 1.

4. If b > 0 and a > 0, then c = a * b.

#### action输出

Before giving the definition of the "output of an action", the `call` method should be paid more attention
on. As is mentioned above, actions includes but is not limited to, performing certain operations
on certain components, producing certain outputs, performing data transformation, and performing
thread scheduling. Among them, the actions performing data transformation are called the "lifting actions".

在给出“action输出”的定义之前，我们把更多的注意放在`call`方法上。上文中提到，action包括但不限于，在特定的组件上执行特定的操作，产生特定的输出，执行数据转换，以及执行线程调度。执行数据转换的action叫做“lifting action”。

The "output of an action" is a group ("output group") of objects, produced in the following way:

一个“action的输出”指的是以下列方式产生的一组对象（输出组）：

1. If the action is not a "lifting action", then the output is exactly the same as the input. 如果action不是“lifting action”，那么输出和输入完全相同。

2. If the action is a "lifting action", then the output is produced according to the effect of various
   actions. And the number of the objects contained in an output group may be different from the number
   of the objects contained in an input group. 如果action是“lifting action”，那么输出是根据不同action的效果产生的。输出组包含的对象数目和输入组包含的对象数目可能不同。

#### 类型

The "type of the input" is the Java type of elements in the input group.

“输入类型”指的是输入组中的元素的Java类型。

The "type of the output" is the Java type of elements in the output group.

“输出类型”指的是输出组中的元素的Java类型。

#### 数据流

Once invoked, a Domino performs a sequence of actions.

一旦被调用，一个Domino执行一个action序列。

The "input of a Domino" is the input of the first action of the action sequence of the Domino.

一个“Domino的输入”是这个Domino的action序列中第一个action的输入。

The "output of a Domino" is the output of the last action of the action sequence of the Domino.

一个“Domino的输出”是这个Domino的action序列中最后一个action的输出。

In an action sequence, the output of a previous action is passed to the next one.
Thus the output of a previous action is exactly the same as the input of the next one.

在一个action序列中，前一个action的输出被传递给下一个action，所以前一个action的输出和下一个action的输入完全相同。

Therefore, the "data flow of a Domino" is the sequence of the input of each action of the Domino,
followed by the output of the Domino.

因此，一个“Domino的数据流”是一个以这个Domino每个action的输入组成并且结尾附上这个Domino的输出的序列。
