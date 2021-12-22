import { HelloScreen } from './helloScreen';

export interface DefaultScreenProps {
  changeScreen: (newScreen: HelloScreen) => void;
}
