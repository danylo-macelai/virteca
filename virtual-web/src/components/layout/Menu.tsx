/*
 * Description: Componente de Menu do projeto
 * Project: virtual-web
 *
 * author: renatoaraujo
 * date: Fri Jul 26 2019 9:42:40 PM
 * version $
 */

import React from 'react';

import './scss/Menu.scss';
import { Link } from 'react-router-dom';

class Menu extends React.Component {
  render() {
    return (
      <nav className="menu main-menu">
        <ul className="ui list horizontal link">
          <li className="item">
            <Link to="/">Home</Link>
          </li>
          <li className="item">
            <a href="/">Sobre</a>
          </li>
          <li className="item">
            <a href="/">Como funcionamos</a>
          </li>
        </ul>
      </nav>
    );
  }
}

export default Menu;
