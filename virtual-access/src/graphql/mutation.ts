/**
 * Description:</b> FIXME: Document this type <br>
 * Project:</b> virtual-access <br>
 *
 * author: macelai
 * date: 14 de mai de 2019
 * version $
 */

import merge from 'lodash.merge';

import { usuarioMutations } from './schema/usuario.schema';

const Mutation = merge(usuarioMutations);

export { Mutation };
