/*
 * Copyright (C) 2023-2024 Jyguy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>. 
 */

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

rootProject.name = "FasterCrystals"

include("api")
include("core")
include("ver:v1_17")
include("ver:v1_18")
include("ver:v1_18_2")
include("ver:v1_19")
include("ver:v1_19_3")
include("ver:v1_19_4")
include("ver:v1_20")
include("ver:v1_20_2")
include("ver:v1_20_3")
include("ver:v1_20_5")
