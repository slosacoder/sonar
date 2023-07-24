/*
 * Copyright (C) 2023 Sonar Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.api.database;

import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.config.SonarConfiguration;

import java.util.Collection;

public interface Database {
  String VERIFIED_TABLE = "verified";
  String IP_COLUMN = "ip_address";

  void initialize(final @NotNull SonarConfiguration config);

  void purge();

  void dispose();

  Collection<String> getListFromTable(final @NotNull String table,
                                      final @NotNull String column);

  void addListToTable(final @NotNull String table,
                      final @NotNull String column,
                      final @NotNull Collection<String> collection);

  void clear(final @NotNull String table);
}