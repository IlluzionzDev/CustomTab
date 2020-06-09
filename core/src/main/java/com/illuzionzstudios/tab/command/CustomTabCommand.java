/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.tab.command;

import com.illuzionzstudios.mist.command.SpigotCommandGroup;
import com.illuzionzstudios.mist.command.type.ReloadCommand;

/**
 * Main tab command. We mainly just have the reload command here
 */
public class CustomTabCommand extends SpigotCommandGroup {

    @Override
    public void registerSubcommands() {
        registerSubCommand(new ReloadCommand());
    }

}
