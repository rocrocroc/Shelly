#More kinds of Dominoes

The Domino class provides many basic methods. Also you can write derived Dominoes which extend the
class. In the Shelly library, there are already several kinds of derived Dominoes, which are shown
below.

##Task Domino

The Shelly library provides a method for executing a task and processing the result according to the
result or the failure of the task after its execution.

Here is an example.

```
        // Create a domino labeled "LoadingBitmap" which takes a String as input,
        // which is the path of a bitmap.
        Shelly.<String>createDomino("LoadingBitmap")
                // The following actions will be performed in background.
                .background()
                // Execute a task which loads a bitmap according to the path.
                .beginTask(new Task<String, Bitmap, Exception>() {
                    private Bitmap load(String path) throws IOException {
                        if (path == null) {
                            throw new IOException();
                        } else {
                            return null;
                        }
                    }
                    @Override
                    protected void onExecute(String input) {
                        // We load the bitmap.
                        // Remember to call Task.notifySuccess() or Task.notifyFailure() in the end.
                        // Otherwise, the Domino gets stuck here.
                        try {
                            Bitmap bitmap = load(input);
                            notifySuccess(bitmap);
                        } catch (IOException e) {
                            notifyFailure(e);
                        }
                    }
                })
                // The following performs different actions according to the result or the failure
                // of the task.

                // If the execution of the above task succeeds, perform an action.
                .onSuccess(new Action0() {
                    @Override
                    public void call() {
                        // Do something.
                    }
                })
                // If the execution of the above task succeeds,
                // perform an action which takes a bitmap as input.
                .onSuccess(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap input) {
                        // Do something.
                    }
                })
                // The following actions will be performed in the main thread, i.e. the UI thread.
                .uiThread()
                // If the execution of the above task succeeds,
                // perform an action on all registered instances of ImageView.
                .onSuccess(ImageView.class, new TargetAction1<ImageView, Bitmap>() {
                    @Override
                    public void call(ImageView imageView, Bitmap input) {
                        // Do something.
                    }
                })
                // The following actions will be performed in background.
                .background()
                // If the execution of the above task fails, perform an action.
                .onFailure(new Action0() {
                    @Override
                    public void call() {
                        // Do something.
                    }
                })
                // If the execution of the above task fails, print the stack trace fo the exception.
                .onFailure(new Action1<Exception>() {
                    @Override
                    public void call(Exception input) {
                        input.printStackTrace();
                    }
                })
                // If the execution of the above task fails,
                // perform an action on all registered instances of ImageView.
                .onFailure(ImageView.class, new TargetAction1<ImageView, Exception>() {
                    @Override
                    public void call(ImageView imageView, Exception input) {
                        // Do something.
                    }
                })
                .endTask()
                .commit();

```

You may find that after the execution of the task, the result or the exception will be passed to
the following actions, but the original input of the task has been lost. Sometimes we need to know
the original input in the following actions. So you can execute a task using another method.

```
        // Create a domino labeled "LoadingBitmap" which takes a String as input,
        // which is the path of a bitmap.
        Shelly.<String>createDomino("LoadingBitmap 2")
                // The following actions will be performed in background.
                .background()
                // Execute a task which loads a bitmap according to the path.
                .beginTaskKeepingInput(new Task<String, Bitmap, Exception>() {
                    private Bitmap load(String path) throws IOException {
                        if (path == null) {
                            throw new IOException();
                        } else {
                            return null;
                        }
                    }
                    @Override
                    protected void onExecute(String input) {
                        // We load the bitmap.
                        // Remember to call Task.notifySuccess() or Task.notifyFailure() in the end.
                        // Otherwise, the Domino gets stuck here.
                        try {
                            Bitmap bitmap = load(input);
                            notifySuccess(bitmap);
                        } catch (IOException e) {
                            notifyFailure(e);
                        }
                    }
                })
                // The following performs different actions according to the result or the failure
                // of the task.

                // If the execution of the above task succeeds, perform an action.
                .onSuccess(new Action0() {
                    @Override
                    public void call() {
                        // Do something.
                    }
                })
                // If the execution of the above task succeeds,
                // perform an action which takes a bitmap as input.
                .onSuccess(new Action1<Pair<String, Bitmap>>() {
                    @Override
                    public void call(Pair<String, Bitmap> input) {

                    }
                })
                // The following actions will be performed in the main thread, i.e. the UI thread.
                .uiThread()
                // If the execution of the above task succeeds,
                // perform an action on all registered instances of ImageView.
                .onSuccess(ImageView.class, new TargetAction1<ImageView, Pair<String,Bitmap>>() {
                    @Override
                    public void call(ImageView imageView, Pair<String, Bitmap> input) {

                    }
                })
                // The following actions will be performed in background.
                .background()
                // If the execution of the above task fails, perform an action.
                .onFailure(new Action0() {
                    @Override
                    public void call() {
                        // Do something.
                    }
                })
                // If the execution of the above task fails, print the stack trace fo the exception.
                .onFailure(new Action1<Exception>() {
                    @Override
                    public void call(Exception input) {
                        input.printStackTrace();
                    }
                })
                // If the execution of the above task fails,
                // perform an action on all registered instances of ImageView.
                .onFailure(ImageView.class, new TargetAction1<ImageView, Exception>() {
                    @Override
                    public void call(ImageView imageView, Exception input) {
                        // Do something.
                    }
                })
                .endTask()
                // Now the task ends, but the result remains. You can do more in the following.
                .target(new Action1<Pair<String, Bitmap>>() {
                          @Override
                          public void call(Pair<String, Bitmap> input) {

                          }
                })
                .commit();
```

Note that TaskDomino.endTask() will keep the result of the task, thus you can perform more actions
after the execution. See the above for example.

##Retrofit Domino

The Shelly library provides a method for using Retrofit to send a HTTP request and processing the
result according to the result or the failure of the request.

Suppose that we want to use Retrofit to send a HTTP request to get the user information.

```
        Shelly.<String>createDomino("GETTING_USER")
                .background()
                // Return a call for the Retrofit task.
                .beginRetrofitTask(new RetrofitTask<String, User>() {
                    @Override
                    protected Call<User> getCall(String s) {
                        return network.getUser(s);
                    }
                })
                .uiThread()
                // If the request succeed and we get the user information,
                // perform an action.
                .onSuccessResult(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                // If the request succeed and we get the user information,
                // perform an action on MyActivity.
                .onSuccessResult(MyActivity.class, new TargetAction1<MyActivity, User>() {
                    @Override
                    public void call(MyActivity mainActivity, User input) {

                    }
                })
                // If the request succeed but we get an error from the server,
                // perform an action.
                .onResponseFailure(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                // If the request succeed but we get an error from the server,
                // perform an action on MyActivity.
                .onResponseFailure(MyActivity.class, new TargetAction1<MyActivity, Response<User>>() {
                    @Override
                    public void call(MyActivity myActivity, Response<User> input) {

                    }
                })
                // If the request fails, perform an action.
                .onFailure(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable input) {

                    }
                })
                // If the request fails, perform an action on MyActivity.
                .onFailure(MyActivity.class, new TargetAction1<MyActivity, Throwable>() {
                    @Override
                    public void call(MyActivity myActivity, Throwable input) {

                    }
                })
                .endTask()
                .commit();
```

Also, you may find that after the execution of the request, the result or the exception will be
passed to the following actions, but the original input of the task has been lost. Sometimes we
need to know the original input in the following actions. So you can execute a task using another
method.

```
        Shelly.<String>createDomino("GETTING_USER")
                .background()
                // Return a call for the Retrofit task.
                .beginRetrofitTaskKeepingInput(new RetrofitTask<String, User>() {
                    @Override
                    protected Call<User> getCall(String s) {
                        return network.getUser(s);
                    }
                })
                .uiThread()
                // If the request succeed and we get the user information,
                // perform an action.
                .onSuccessResult(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                // If the request succeed and we get the user information,
                // perform an action on MyActivity.
                .onSuccessResult(MyActivity.class, new TargetAction2<MyActivity, String, User>() {
                    @Override
                    public void call(MyActivity myActivity, String input1, User input2) {

                    }
                })
                // If the request succeed but we get an error from the server,
                // perform an action.
                .onResponseFailure(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                // If the request succeed but we get an error from the server,
                // perform an action on MyActivity.
                .onResponseFailure(MyActivity.class, new TargetAction2<MyActivity, String, Response<User>>() {
                    @Override
                    public void call(MyActivity myActivity, String input1, Response<User> input2) {

                    }
                })
                // If the request fails, perform an action.
                .onFailure(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable input) {

                    }
                })
                // If the request fails, perform an action on MyActivity.
                .onFailure(MyActivity.class, new TargetAction1<MyActivity, Throwable>() {
                    @Override
                    public void call(MyActivity myActivity, Throwable input) {

                    }
                })
                .endTask()
                .commit();
```

##The merging and combination of Dominoes

Dominoes can be merged. If we merge two Dominoes whose final data are of the same type, then we get
a new Domino whose initial data is the union of their final data.

```
        Shelly.<String>createDomino("Find *.jpg")
                .background()
                .map(new Function1<String, File>() {
                    @Override
                    public File call(String input) {
                        return new File(input);
                    }
                })
                .flatMap(new Function1<File, List<Bitmap>>() {
                    @Override
                    public List<Bitmap> call(File input) {
                        // Find *.jpg in this folder
                        return null;
                    }
                })
                .commit();
        Shelly.<String>createDomino("Find *.png")
                .background()
                .map(new Function1<String, File>() {
                    @Override
                    public File call(String input) {
                        return new File(input);
                    }
                })
                .flatMap(new Function1<File, List<Bitmap>>() {
                    @Override
                    public List<Bitmap> call(File input) {
                        // Find *.png in this folder
                        return null;
                    }
                })
                .commit();
        Shelly.<String>createDomino("Find *.png and *.jpg")
                .background()
                .merge(Shelly.<String, Bitmap>getDominoByLabel("Find *.png"),
                        Shelly.<String, Bitmap>getDominoByLabel("Find *.jpg"))
                .uiThread()
                .target(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap input) {

                    }
                })
                .commit();
```

Also, you can write the following using anonymous Dominoes.

```
        Shelly.<String>createDomino("Find *.png and *.jpg")
                .background()
                .merge(Shelly.<String>createDomino()
                                .background()
                                .map(new Function1<String, File>() {
                                    @Override
                                    public File call(String input) {
                                        return new File(input);
                                    }
                                })
                                .flatMap(new Function1<File, List<Bitmap>>() {
                                    @Override
                                    public List<Bitmap> call(File input) {
                                        // Find *.jpg in this folder
                                        return null;
                                    }
                                }),
                        Shelly.<String>createDomino()
                                .background()
                                .map(new Function1<String, File>() {
                                    @Override
                                    public File call(String input) {
                                        return new File(input);
                                    }
                                })
                                .flatMap(new Function1<File, List<Bitmap>>() {
                                    @Override
                                    public List<Bitmap> call(File input) {
                                        // Find *.png in this folder
                                        return null;
                                    }
                                })
                )
                .uiThread()
                .target(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap input) {

                    }
                })
                .commit();
```

You can combine two Dominoes into one. Specifically, suppose there are two Dominoes named "Domino A"
and "Domino B", and you can provide a function and combine these two Dominoes into a new Domino
named "Domino C" in the following way: The final data of the two Dominoes are passed into th
function and the function returns new data as the input of Domino C.

The following is an example.

```
        Shelly.<String>createDomino("Login")
                .combine(
                        Shelly.<String>createDomino()
                                .beginRetrofitTask(new RetrofitTask<String, User>() {
                                    @Override
                                    protected Call<User> getCall(String s) {
                                        return network.getUser(s);
                                    }
                                })
                                .endTask(),
                        Shelly.<String>createDomino()
                                .beginRetrofitTask(new RetrofitTask<String, Summary>() {
                                    @Override
                                    protected Call<Summary> getCall(String s) {
                                        return network.getSummary(s);
                                    }
                                })
                                .endTask(),
                        new Function2<User, Summary, Internal>() {
                            @Override
                            public Internal call(User input1, Summary input2) {
                                return null;
                            }
                        }
                )
                .target(new Action1<Internal>() {
                    @Override
                    public void call(Internal input) {

                    }
                })
                .commit();
```

###Domino invocation within a Domino

The Shelly library provides the methods for invoking Dominoes.

The following is an example.

```
        Shelly.<String>createDomino("Login")
                .beginRetrofitTask(new RetrofitTask<String, User>() {
                    @Override
                    protected Call<User> getCall(String s) {
                        return network.getUser(s);
                    }
                })
                .onSuccessResult(
                        Shelly.<User>createDomino()
                                .beginRetrofitTask(new RetrofitTask<User, Summary>() {
                                    @Override
                                    protected Call<Summary> getCall(User user) {
                                        return network.getSummary(user.getId());
                                    }
                                })
                                .onSuccessResult(new Action1<Summary>() {
                                    @Override
                                    public void call(Summary input) {

                                    }
                                })
                                .endTask()
                )
                .endTask()
                .commit();
```

###Tuple input

You may find that in the above examples, all of the Dominoes take only one input each time, but how
about multiple input?

The Shelly library provides you with some tuple classes, in which you can put multiple inputs.

The following is an example.

```
        Shelly.<Triple<Integer, Integer, Double>>createDomino("Add")
                .map(new Function1<Triple<Integer,Integer,Double>, Double>() {
                    @Override
                    public Double call(Triple<Integer, Integer, Double> input) {
                        return input.first + input.second + input.third;
                    }
                })
                .target(new Action1<Double>() {
                    @Override
                    public void call(Double input) {
                        System.out.print(input);
                    }
                })
                .commit();
```

Invoke the Domino with the following statement:

```
Shelly.playDomino("Add", Triple.create(1, 2, 3.0));
```

###Stash

Sometimes, in an action performed by a Domino, you want to save something for later use. Now you can
use the stash actions or stash functions, whose classes are located in the package
`xiaofei.library.shelly.function.stashfunction`. These actions and functions are the same as their
corresponding actions and functions except that they provide additional methods for stashing, with
which you can stash data for later use.

The following is an example:

```
```