package com.loop.game.States;

import com.loop.game.GameModel.Cell;

/**
 * Created by Piotr on 2017-06-06.
 */

public interface PlayScreen
{
    void disableAllButtons();
    void updateMenu(Cell cell);
}
