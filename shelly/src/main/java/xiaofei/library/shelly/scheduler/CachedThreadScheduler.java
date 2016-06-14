/**
 *
 * Copyright 2016 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xiaofei.library.shelly.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xiaofei.library.shelly.internal.Player;

/**
 * Created by Xiaofei on 16/5/31.
 */
public class CachedThreadScheduler extends Scheduler {

    private static ExecutorService sExecutorService = Executors.newCachedThreadPool();

    public CachedThreadScheduler(Object input) {
        super(input);
    }

    @Override
    protected void onPlay(final Player player, final Object input) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                player.play(input);
            }
        });
    }
}
