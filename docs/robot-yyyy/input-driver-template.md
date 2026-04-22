<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to the robot's
     own src/main/java/<your-package>/subsystems/Input/InputDriver.java.
     Swap the extended base class (InputXBox, InputPS4, etc.) to
     match the controller hardware. Add one semantic getter per
     input per subsystem. -->
<!-- markdownlint-disable MD013 -->

# Input-Driver Template

Every robot-project input class routes raw operator input through
one place that owns port assignment, button mapping, and
connection-state handling. Subsystem code calls semantic getters
(`getDisableSensors()`), never raw button indices.

```java
package com.example.app.subsystems.Input;

import com.team271.lib.TObj;
import com.team271.lib.hardware.Input.InputXBox;
import com.example.app.Constants;
import edu.wpi.first.wpilibj.XboxController;

public class InputDriver extends InputXBox {

    public InputDriver(final TObj argParent) {
        super(argParent, "Driver", Constants.Controller.DRIVER_PORT);
    }

    /*
     * Sensor Control
     */
    @Override
    public boolean getDisableSensors() {
        boolean result = false;
        if (mController.isConnected()) {
            result = buttons[XboxController.Button.kBack.value - 1];
        }
        return result;
    }

    @Override
    public boolean getEnableSensors() {
        boolean result = false;
        if (mController.isConnected()) {
            result = buttons[XboxController.Button.kStart.value - 1];
        }
        return result;
    }

    /*
     * Subsystem-specific controls -- add one semantic getter per
     * input per subsystem. Name by intent, not by button.
     */
}
```

## Conventions demonstrated

1. Extends a library input base class (`InputXBox`, `InputPS4`, etc.)
   so deadband, polling, and simulation hooks come from one place.
2. Constructor takes `TObj argParent` and routes through the library
   base; port comes from `Constants.Controller`, not a magic number.
3. **Connection guard** (`mController.isConnected()`) on every read --
   an unplugged controller returns a safe default, not a stale bit
   (CODE-GEN-008).
4. **Semantic getters** (`getDisableSensors()`) keep button-to-action
   mapping in one file. Subsystem code never sees raw button indices.
5. `-1` offset when indexing the inherited `buttons[]` array --
   WPILib numbers buttons 1-indexed, but the array is 0-indexed.

## Adjacent robot-project requirement

The same pattern applies to `InputOperator`, `InputCoach`, or any
other role-specific controller. Each role gets its own class
extending the same library base; `Constants.Controller` holds the
port assignments.
